package twitterx.video.api

public interface VideoService {
    public suspend fun downloadVideo(url: String): Result<VideoDownloadResult>
    public fun isSupported(url: String): Boolean
    public fun getSupportedPlatforms(): List<VideoPlatform>
    public fun clearFolder(): Result<Unit>
}
