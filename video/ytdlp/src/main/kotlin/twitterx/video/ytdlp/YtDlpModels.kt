package twitterx.video.ytdlp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import twitterx.video.api.VideoPlatform
import java.time.Duration

public data class YtDlpConfig(
    val executablePath: String,
    val outputFormat: String = "mp4",
    val maxFileSize: String = "50M",
    val videoPath: String = ".tmp/videos",
    val timeout: Duration = Duration.ofMinutes(2),
    val cleanupDuration: Duration = Duration.ofMinutes(5),
    val cookiesFile: String
)

public data class YtDlpResult(
    val success: Boolean,
    val exitCode: Int,
    val output: String,
    val error: String
)

@Serializable
public data class YtDlpVideoInfo(
    val id: String? = null,
    val title: String? = null,
    val duration: Double? = null,
    val uploader: String? = null,
    val format: String? = null,
    val filesize: Long? = null,
    val ext: String? = null,
    val url: String? = null,
    @SerialName("webpage_url") val webpageUrl: String? = null,
    val extractor: String? = null
)

public enum class PlatformPattern(
    public val pattern: Regex,
    public val platform: VideoPlatform
) {
    YOUTUBE_SHORTS(
        Regex("https?://(?:www\\.)?youtube\\.com/shorts/[\\w-]+", RegexOption.IGNORE_CASE),
        VideoPlatform.YOUTUBE_SHORTS
    ),
    TIKTOK(
        Regex("https://.*\\.tiktok\\.com/.*", RegexOption.IGNORE_CASE),
        VideoPlatform.TIKTOK
    ),
    INSTAGRAM_REELS(
        Regex("https://www\\.instagram\\.com/reels?/[\\w-]+", RegexOption.IGNORE_CASE),
        VideoPlatform.INSTAGRAM_REELS
    ),
}

public object PlatformDetector {
    public fun detectPlatform(url: String): VideoPlatform {
        return PlatformPattern.entries
            .firstOrNull { it.pattern.containsMatchIn(url) }
            ?.platform
            ?: VideoPlatform.UNKNOWN
    }

    public fun isSupported(url: String): Boolean {
        return detectPlatform(url) != VideoPlatform.UNKNOWN
    }
}
