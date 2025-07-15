package twitterx.video.api

public sealed class VideoException(
    message: String,
    cause: Throwable? = null
) :
    Exception(message, cause) {
    public class UnsupportedVideoUrlException(url: String) : VideoException("Unsupported video URL: $url")

    public class VideoDownloadException(
        url: String,
        cause: Throwable? = null
    ) : VideoException(
        "Failed to download video from: $url",
        cause
    )

    public class VideoProcessingException(
        message: String,
        cause: Throwable? = null
    ) : VideoException(
        "Video processing failed: $message",
        cause
    )

    public class VideoFileTooLargeException(
        size: Long,
        maxSize: Long
    ) : VideoException(
        "Video file too large: $size bytes, max allowed: $maxSize bytes"
    )

    public class VideoProcessingTimeoutException(
        timeout: java.time.Duration
    ) : VideoException(
        "Video processing timeout after: $timeout"
    )

    public class CookiesFileNotFoundException(
        cookiesFile: String
    ) : VideoException("Cookies file not found: $cookiesFile")

    public class YtDlpNotInstalledException(
        executablePath: String,
        cause: Throwable? = null
    ) : VideoException("yt-dlp not found at: $executablePath. Please install yt-dlp.", cause)

    public class UnsupportedPlatformException(
        platform: String
    ) : VideoException("Unsupported video platform: $platform")
}
