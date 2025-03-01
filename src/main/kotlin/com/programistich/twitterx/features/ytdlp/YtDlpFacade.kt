package com.programistich.twitterx.features.ytdlp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import java.util.UUID
import java.util.logging.Logger

@Component
class YtDlpFacade(
    @Value("\${yt-dlp.path}") private val ytDlpPath: String
) {
    private val logger = Logger.getLogger(this::class.java.name)
    private val deleteScope = CoroutineScope(Dispatchers.IO)
    private val videoFolder = File("video").apply {
        deleteRecursively()
        mkdir()
    }

    fun download(url: String): File {
        logger.info("Downloading video from $url")
        val downloadFolder = File(videoFolder, UUID.randomUUID().toString()).apply { mkdir() }

        val filename = UUID.randomUUID().toString()
        val videoFile = File(downloadFolder, "$filename.mp4")

        val commands = listOf(
            ytDlpPath,
            "-v",
            url,
            "-o",
            videoFile.absolutePath
        )
        logger.info("Running yt-dlp with command: ${commands.joinToString(" ")}")

        val process = ProcessBuilder(commands).start().apply { waitFor() }
        val result = String(process.errorStream.readAllBytes())

        logger.info("yt-dlp result: $result")
        logger.info("Finished downloading video from $url")

        check(videoFile.exists()) { "yt-dlp failed to download video" }
        check(process.exitValue() == 0) { "yt-dlp failed to download video" }

        deleteScope.launch {
            delay(TIME_TO_LIVE)
            downloadFolder.deleteRecursively()
        }

        return videoFile
    }

    companion object {
        const val TIME_TO_LIVE = 1000 * 60 * 5L // 5 minutes
    }
}