package twitter.app.features.twitter

import twitterx.twitter.api.Tweet

/**
 * Processor for handling media in inline mode according to specific rules.
 */
public object InlineMediaProcessor {

    /**
     * Result of media processing for inline mode.
     */
    public data class InlineMediaResult(
        val photoUrl: String? = null,
        val hasMedia: Boolean = false
    )

    /**
     * Process tweet media for inline mode according to the following rules:
     * 1. If there are multiple photos (mosaic exists) - use mosaic JPEG
     * 2. If there are videos and photos - use only the last photo
     * 3. If there are only videos - send only text (no media)
     * 4. If there are only photos - use the last photo
     */
    public fun processMediaForInline(tweet: Tweet): InlineMediaResult {
        val hasPhotos = tweet.mediaUrls.isNotEmpty()
        val hasVideos = tweet.videoUrls.isNotEmpty()

        return when {
            // Rule 1: Multiple photos (mosaic) - handled by FXTwitterConverter
            // If mosaic exists, it's already in mediaUrls as the last item
            hasPhotos && !hasVideos -> {
                InlineMediaResult(
                    photoUrl = tweet.mediaUrls.lastOrNull(),
                    hasMedia = true
                )
            }

            // Rule 2: Videos and photos - use only the last photo
            hasPhotos && hasVideos -> {
                InlineMediaResult(
                    photoUrl = tweet.mediaUrls.lastOrNull(),
                    hasMedia = true
                )
            }

            // Rule 3: Only videos - send only text (no media)
            !hasPhotos && hasVideos -> {
                InlineMediaResult(
                    photoUrl = null,
                    hasMedia = false
                )
            }

            // Rule 4: No media at all
            else -> {
                InlineMediaResult(
                    photoUrl = null,
                    hasMedia = false
                )
            }
        }
    }
}
