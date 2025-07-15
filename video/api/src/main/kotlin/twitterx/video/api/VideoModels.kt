package twitterx.video.api

import java.io.File
import java.time.Duration

public data class VideoDownloadResult(
    val file: File,
    val metadata: VideoMetadata,
    val timeToLive: Duration = Duration.ofMinutes(DEFAULT_TTL_MINUTES)
) {
    public companion object {
        private const val DEFAULT_TTL_MINUTES = 5L
    }
}

public data class VideoMetadata(
    val title: String? = null,
    val duration: Duration? = null,
    val format: String? = null,
    val size: Long? = null,
    val platform: VideoPlatform
)

public enum class VideoPlatform {
    YOUTUBE_SHORTS,
    TIKTOK,
    INSTAGRAM_REELS,
    UNKNOWN
}
