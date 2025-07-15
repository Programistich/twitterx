package twitterx.video.ytdlp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import twitterx.video.api.VideoDownloadResult
import twitterx.video.api.VideoException
import twitterx.video.api.VideoMetadata
import twitterx.video.api.VideoPlatform
import twitterx.video.api.VideoService
import java.io.File
import java.time.Duration
import java.util.UUID
import java.util.concurrent.TimeUnit

public class YtDlpVideoService(
    private val config: YtDlpConfig
) : VideoService {

    private companion object {
        private const val BYTES_PER_KB = 1024L
        private const val BYTES_PER_MB = BYTES_PER_KB * 1024L
        private const val BYTES_PER_GB = BYTES_PER_MB * 1024L
        private const val COMMAND_NOT_FOUND_EXIT_CODE = 127
        private val logger: Logger = LoggerFactory.getLogger(YtDlpVideoService::class.java)
    }
    private val deleteScope = CoroutineScope(Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }
    private val commandBuilder = YtDlpCommandBuilder(config)
    private val processExecutor = YtDlpProcessExecutor(config)

    private val videoFolder = File(config.videoPath).apply {
        if (exists()) deleteRecursively()
        mkdirs()
    }

    @Volatile
    private var isYtDlpChecked = false

    override suspend fun downloadVideo(url: String): Result<VideoDownloadResult> {
        if (!isSupported(url)) {
            logger.warn("Unsupported video URL: {}", url)
            return Result.failure(VideoException.UnsupportedVideoUrlException(url))
        }

        return withContext(Dispatchers.IO) {
            runCatching {
                logger.debug("Starting video download process: url={}", url)
                ensureYtDlpInstalled()
                validateCookiesFile()

                logger.info("Starting video download from: {}", url)

                val videoFile = createVideoFile()
                val videoInfo = getVideoInfo(url)

                validateFileSize(videoInfo)

                val downloadResult = processExecutor.execute(commandBuilder.buildDownloadCommand(url, videoFile))

                if (!downloadResult.success) {
                    logger.error("yt-dlp download failed: url={}, error={}", url, downloadResult.error)
                    throw VideoException.VideoProcessingException("yt-dlp download failed: ${downloadResult.error}")
                }

                if (!videoFile.exists()) {
                    logger.error(
                        "Video file not created after download: url={}, expectedPath={}",
                        url,
                        videoFile.absolutePath
                    )
                    throw VideoException.VideoProcessingException("Video file not created after download")
                }

                scheduleCleanup(videoFile)

                val metadata = VideoMetadata(
                    title = videoInfo.title,
                    duration = videoInfo.duration?.let { Duration.ofSeconds(it.toLong()) },
                    format = videoInfo.format,
                    size = videoFile.length(),
                    platform = PlatformDetector.detectPlatform(url)
                )

                logger.info(
                    "Video download completed successfully: url={}, file={}, size={} bytes",
                    url,
                    videoFile.absolutePath,
                    videoFile.length()
                )

                VideoDownloadResult(
                    file = videoFile,
                    metadata = metadata,
                    timeToLive = config.cleanupDuration
                )
            }.onFailure { exception ->
                logger.error("Video download failed: url={}", url, exception)
            }
        }
    }

    override fun isSupported(url: String): Boolean = PlatformDetector.isSupported(url)

    override fun getSupportedPlatforms(): List<VideoPlatform> = listOf(
        VideoPlatform.YOUTUBE_SHORTS,
        VideoPlatform.TIKTOK,
        VideoPlatform.INSTAGRAM_REELS,
    )

    override fun clearFolder(): Result<Unit> = runCatching {
        if (videoFolder.exists()) {
            videoFolder.deleteRecursively()
            logger.info("Cleared video folder: {}", videoFolder.absolutePath)
        } else {
            logger.warn("Video folder does not exist: {}", videoFolder.absolutePath)
        }
    }.onFailure { exception ->
        logger.error("Failed to clear video folder: {}", videoFolder.absolutePath, exception)
    }

    public suspend fun checkYtDlpInstallation(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            logger.info("Checking yt-dlp installation at: {}", config.executablePath)

            val command = listOf(config.executablePath, "--version")
            logger.debug("Check command: {}", command.joinToString(" "))

            val result = processExecutor.execute(command)

            if (!result.success) {
                val errorMessage = if (result.error.contains("command not found") ||
                    result.error.contains("is not recognized") ||
                    result.exitCode == COMMAND_NOT_FOUND_EXIT_CODE
                ) {
                    "yt-dlp executable not found"
                } else {
                    "yt-dlp version check failed: ${result.error}"
                }
                logger.error("yt-dlp installation check failed: {}", errorMessage)
                throw VideoException.YtDlpNotInstalledException(
                    config.executablePath,
                    RuntimeException(errorMessage)
                )
            }

            val version = result.output.trim()
            logger.info("yt-dlp version: {}", version)
            version
        }.recoverCatching { exception ->
            when (exception) {
                is VideoException.YtDlpNotInstalledException -> throw exception
                else -> {
                    logger.error("Unexpected error during yt-dlp installation check", exception)
                    throw VideoException.YtDlpNotInstalledException(config.executablePath, exception)
                }
            }
        }
    }

    public suspend fun getVideoInfo(url: String): YtDlpVideoInfo = withContext(Dispatchers.IO) {
        logger.debug("Getting video info: url={}", url)
        ensureYtDlpInstalled()

        val commands = commandBuilder.buildInfoCommand(url)

        logger.debug("Getting video info with command: {}", commands.joinToString(" "))

        val result = withTimeout(config.timeout.toMillis()) {
            processExecutor.execute(commands)
        }

        if (!result.success) {
            logger.warn("Failed to get video info: url={}, error={}", url, result.error)
            return@withContext createFallbackVideoInfo(url)
        }

        try {
            val videoInfo = json.decodeFromString<YtDlpVideoInfo>(result.output)
            logger.info("Successfully retrieved video info: url={}, title={}", url, videoInfo.title)
            videoInfo
        } catch (exception: Exception) {
            logger.warn("Failed to parse video info JSON: url={}", url, exception)
            createFallbackVideoInfo(url)
        }
    }

    private suspend fun ensureYtDlpInstalled() {
        if (!isYtDlpChecked) {
            checkYtDlpInstallation().getOrThrow()
            isYtDlpChecked = true
        }
    }

    private fun validateCookiesFile() {
        if (!File(config.cookiesFile).exists()) {
            logger.error("Cookies file not found: {}", config.cookiesFile)
            throw VideoException.CookiesFileNotFoundException("Cookies file not found: ${config.cookiesFile}")
        }
        logger.debug("Cookies file validated: {}", config.cookiesFile)
    }

    private fun createVideoFile(): File {
        val videoFile = File(videoFolder, "${UUID.randomUUID()}.${config.outputFormat}")
        logger.debug("Created video file path: {}", videoFile.absolutePath)
        return videoFile
    }

    private fun validateFileSize(videoInfo: YtDlpVideoInfo) {
        videoInfo.filesize?.let { size ->
            val maxSizeInBytes = config.maxFileSize.toBytes()
            if (size > maxSizeInBytes) {
                logger.warn("Video file too large: size={} bytes, maxSize={} bytes", size, maxSizeInBytes)
                throw VideoException.VideoFileTooLargeException(size, maxSizeInBytes)
            }
            logger.debug("Video file size validation passed: size={} bytes, maxSize={} bytes", size, maxSizeInBytes)
        }
    }

    private fun scheduleCleanup(file: File) {
        logger.debug("Scheduling cleanup for file: {}, duration={}", file.absolutePath, config.cleanupDuration)
        deleteScope.launch {
            delay(config.cleanupDuration.toMillis())
            val result = file.delete()
            if (result) {
                logger.info("Scheduled cleanup completed for file: {}", file.absolutePath)
            } else {
                logger.warn("Failed to delete file: {}", file.absolutePath)
            }
        }
    }

    private fun createFallbackVideoInfo(url: String): YtDlpVideoInfo {
        logger.debug("Creating fallback video info for URL: {}", url)
        return YtDlpVideoInfo(
            title = "Unknown",
            format = config.outputFormat,
            extractor = PlatformDetector.detectPlatform(url).name.lowercase()
        )
    }

    private fun String.toBytes(): Long = when {
        endsWith("G", ignoreCase = true) -> removeSuffix("G").removeSuffix("g").toLong() * BYTES_PER_GB
        endsWith("M", ignoreCase = true) -> removeSuffix("M").removeSuffix("m").toLong() * BYTES_PER_MB
        endsWith("K", ignoreCase = true) -> removeSuffix("K").removeSuffix("k").toLong() * BYTES_PER_KB
        else -> toLong()
    }

    private class YtDlpCommandBuilder(private val config: YtDlpConfig) {
        fun buildDownloadCommand(url: String, outputFile: File): List<String> = buildList {
            add(config.executablePath)
            add("--output")
            add(outputFile.absolutePath)

            val platform = PlatformDetector.detectPlatform(url)
            when (platform) {
                VideoPlatform.YOUTUBE_SHORTS -> {
                    add("--format")
                    add("bestvideo[height<=1080]+bestaudio")
                    add("-S")
                    add("proto,ext:mp4:m4a,res,br")
                }
                VideoPlatform.TIKTOK, VideoPlatform.INSTAGRAM_REELS -> {
                    add("-S")
                    add("proto,ext:mp4:m4a,res,br")
                }
                else -> throw VideoException.UnsupportedPlatformException(
                    "Unsupported platform: $platform for URL: $url"
                )
            }

            add("--max-filesize")
            add(config.maxFileSize)
            add("--cookies")
            add(config.cookiesFile)
            add(url)
        }

        fun buildInfoCommand(url: String): List<String> = buildList {
            add(config.executablePath)
            add("--dump-json")
            add("--no-download")
            add("--cookies")
            add(config.cookiesFile)
            add(url)
        }
    }

    private class YtDlpProcessExecutor(private val config: YtDlpConfig) {
        suspend fun execute(commands: List<String>): YtDlpResult = withContext(Dispatchers.IO) {
            val processBuilder = ProcessBuilder(commands)
            processBuilder.redirectErrorStream(false)

            val process = processBuilder.start()
            process.outputStream.close()

            val outputDeferred = async { process.inputStream.bufferedReader().use { it.readText() } }
            val errorDeferred = async { process.errorStream.bufferedReader().use { it.readText() } }

            val processCompleted = process.waitFor(config.timeout.toMillis(), TimeUnit.MILLISECONDS)

            if (!processCompleted) {
                process.destroyForcibly()
                outputDeferred.cancel()
                errorDeferred.cancel()
                throw VideoException.VideoProcessingTimeoutException(config.timeout)
            }

            val output = outputDeferred.await()
            val error = errorDeferred.await()

            YtDlpResult(
                success = process.exitValue() == 0,
                exitCode = process.exitValue(),
                output = output,
                error = error
            )
        }
    }
}
