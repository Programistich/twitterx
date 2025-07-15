# Video Processing Module

A comprehensive video processing module for the TwitterX project that provides video downloading capabilities using yt-dlp.

## Architecture

The module follows a clean architecture pattern with separate API, implementation, and integration layers:

```
video/
├── api/                # Core interfaces and models
├── ytdlp/              # yt-dlp related implementation
└── e2e/                # End-to-end tests
```

## How do I pass cookies to yt-dlp?

Passing cookies to yt-dlp is a good way to workaround login when a particular extractor does not implement it explicitly. 
Another use case is working around CAPTCHA some websites require you to solve in particular cases in order to get access (e.g. YouTube, CloudFlare).
If you wish to manually pass cookies, use the --cookies option, for example: --cookies /path/to/cookies/file.txt.

Command what generates the cookies file:
```
yt-dlp "https://www.instagram.com/reel/DL1RgqLiSlZ/\?utm_source\=ig_web_copy_link" --cookies-from-browser firefox --cookies cookies.txt
```

You can export your cookies to a text file without any third-party software by using yt-dlp's --cookies-from-browser option in conjunction with the --cookies option, for example: yt-dlp --cookies-from-browser chrome --cookies cookies.txt. yt-dlp will extract the browser cookies and save them to the filepath specified after --cookies. The resulting text file can then be used with the --cookies option. Note though that this method exports your browser's cookies for ALL sites (even if you passed a URL to yt-dlp), so take care in not letting this text file fall into the wrong hands.
You may also use a conforming browser extension for exporting cookies, such as Get cookies.txt LOCALLY for Chrome or cookies.txt for Firefox. 

## Modules

### video:api
Core interfaces and models for video processing.

**Key Components:**
- `VideoService` - Main interface for video operations
- `CookiesManager` - Interface for managing cookies files
- `VideoDownloadResult` - Result container for download operations
- `VideoMetadata` - Video metadata information
- `VideoPlatform` - Supported platform enumeration
- `VideoException` - Exception hierarchy for error handling

### video:ytdlp
yt-dlp specific implementation for video downloading.

**Key Components:**
- `YtDlpVideoService` - Main service implementation using yt-dlp
- `CookiesManagerImpl` - Implementation for managing cookies files
- `YtDlpConfig` - Configuration data class for yt-dlp settings
- `PlatformDetector` - Object for URL pattern matching and platform detection
- `YtDlpCommandBuilder` - Internal class for building yt-dlp commands
- `YtDlpProcessExecutor` - Internal class for executing yt-dlp processes
- `YtDlpVideoInfo` - Data class for video metadata from yt-dlp
- `YtDlpResult` - Data class for process execution results
- `PlatformPattern` - Enum with regex patterns for supported platforms

**Supported Platforms:**
- YouTube shorts
  - Command ```yt-dlp -o $NAME -f 'bestvideo[height<=1080]+bestaudio' -S "proto,ext:mp4:m4a,res,br" $URL --cookies cookies.txt```
  - Pattern `https://www.youtube.com/shorts/*` or `https://youtube.com/shorts/*`
- TikTok videos
  - Command ```yt-dlp -o $NAME -S "proto,ext:mp4:m4a,res,br" $URL --cookies cookies.txt```
  - Pattern `https://*.tiktok.com/*`
- Instagram reels
  - Command ```yt-dlp -o $NAME -S "proto,ext:mp4:m4a,res,br" $URL --cookies cookies.txt```
  - Pattern `https://www.instagram.com/reels/*` or `https://instagram.com/reels/*`

Name it is UUID what generates automatically, so you can use it as a file name.

### video:e2e
End-to-end tests for video processing functionality.

## Error Handling

The module provides comprehensive error handling through a hierarchy of exceptions:

- `VideoException` - Base exception
- `UnsupportedVideoUrlException` - URL not supported
- `VideoDownloadException` - Download failure
- `VideoProcessingException` - Processing failure
- `VideoFileTooLargeException` - File size exceeded
- `VideoProcessingTimeoutException` - Operation timeout

## Testing

### Unit Tests
```bash
./gradlew :video:ytdlp:test
```

### End-to-End Tests
```bash
./gradlew :video:e2e:test
```
