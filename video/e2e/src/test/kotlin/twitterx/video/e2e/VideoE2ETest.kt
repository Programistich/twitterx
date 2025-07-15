package twitterx.video.e2e

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import twitterx.video.ytdlp.YtDlpConfig
import twitterx.video.ytdlp.YtDlpVideoService
import java.time.Duration
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VideoE2ETest {

    private fun cookiesFile(): String {
        return this::class.java.getResource("/cookies.txt")?.path!!
    }

    private val ytDlpConfig = YtDlpConfig(
        executablePath = System.getProperty("yt-dlp.path", "yt-dlp"),
        cookiesFile = cookiesFile()
    )

    private val videoService = YtDlpVideoService(ytDlpConfig)

    @Test
    fun `test yt-dlp installation`() = runTest {
        assertTrue(videoService.checkYtDlpInstallation().isSuccess)
    }

    @Test
    fun `test yt-dlp throw installation`() = runTest {
        val ytDlpConfigInvalid = ytDlpConfig.copy(
            executablePath = "invalid/path/to/yt-dlp"
        )
        val videoServiceInvalid = YtDlpVideoService(ytDlpConfigInvalid)
        assertTrue(videoServiceInvalid.checkYtDlpInstallation().isFailure)
    }

    @Test
    fun `test URL support detection`() {
        // YouTube Shorts (supported)
        assertTrue(videoService.isSupported("https://www.youtube.com/shorts/abc123"))

        // Instagram Reels (supported)
        assertTrue(videoService.isSupported("https://www.instagram.com/reel/abc123"))
        assertTrue(videoService.isSupported("https://www.instagram.com/reels/abc123"))

        // TikTok (supported)
        assertTrue(videoService.isSupported("https://www.tiktok.com/@user/video/123456"))
        assertTrue(videoService.isSupported("https://vm.tiktok.com/abc123"))

        // Unsupported URLs
        assertFalse(videoService.isSupported("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
        assertFalse(videoService.isSupported("https://youtu.be/dQw4w9WgXcQ"))
        assertFalse(videoService.isSupported("https://www.instagram.com/p/abc123"))
        assertFalse(videoService.isSupported("https://twitter.com/user/status/123456"))
        assertFalse(videoService.isSupported("https://x.com/user/status/123456"))
        assertFalse(videoService.isSupported("https://www.facebook.com/user/videos/123456"))
        assertFalse(videoService.isSupported("https://example.com/video"))
    }

    @Test
    fun `test video info retrieval`() = runTest {
        val testUrl = "https://www.youtube.com/shorts/sgLRNeznFQk"

        assertTrue(videoService.isSupported(testUrl), "Test URL should be supported")

        val videoInfo = videoService.getVideoInfo(testUrl)

        // Should not be null and have basic info
        assertTrue(videoInfo.title?.isNotEmpty() == true || videoInfo.title == "Unknown")
        assertTrue(videoInfo.extractor?.isNotEmpty() == true)
    }

    @Test
    fun `test video file deletion after processing`() = runBlocking {
        val testConfig = ytDlpConfig.copy(
            timeout = Duration.ofMinutes(2),
            cleanupDuration = Duration.ofMillis(2000)
        )
        val testVideoService = YtDlpVideoService(testConfig)

        val testUrl = "https://www.youtube.com/shorts/sgLRNeznFQk"

        assertTrue(testVideoService.isSupported(testUrl), "Test URL should be supported")

        val result = testVideoService.downloadVideo(testUrl)
        if (result.isFailure) {
            println("Download failed: ${result.exceptionOrNull()}")
            result.exceptionOrNull()?.printStackTrace()
        }
        assertTrue(result.isSuccess, "Download should succeed")

        val videoResult = result.getOrThrow()
        val videoFile = videoResult.file

        assertTrue(videoFile.exists(), "Video file should exist immediately after download")

        delay(4000)

        assertFalse(videoFile.exists(), "Video file should be deleted after cleanup duration")
    }

    @Test
    fun `test supported platforms list`() {
        val supportedPlatforms = videoService.getSupportedPlatforms()

        assertTrue(supportedPlatforms.isNotEmpty(), "Should support at least one platform")
        assertTrue(supportedPlatforms.any { it.name.contains("YOUTUBE") })
        assertTrue(supportedPlatforms.any { it.name.contains("TIKTOK") })
        assertTrue(supportedPlatforms.any { it.name.contains("INSTAGRAM") })
    }

    @Test
    fun `test unsupported URL download`() = runTest {
        val unsupportedUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"

        assertFalse(videoService.isSupported(unsupportedUrl), "URL should not be supported")

        val result = videoService.downloadVideo(unsupportedUrl)
        assertTrue(result.isFailure, "Download should fail for unsupported URL")
    }

    @AfterTest
    fun cleanup() {
        videoService.clearFolder().getOrNull()
    }
}
