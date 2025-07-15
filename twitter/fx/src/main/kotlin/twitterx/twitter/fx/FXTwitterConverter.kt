package twitterx.twitter.fx

import twitterx.twitter.api.Tweet
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

internal object FXTwitterConverter {
    internal fun convert(fxTweet: FxTweet, tweetId: String): Tweet {
        return Tweet(
            id = tweetId,
            username = fxTweet.author.screenName,
            fullName = fxTweet.author.name,
            content = fxTweet.text,
            createdAt = parseCreatedAt(fxTweet.createdAt),
            mediaUrls = extractMediaUrls(fxTweet),
            videoUrls = extractVideoUrls(fxTweet),
            replyToTweetId = fxTweet.replyingToStatus,
            retweetOfTweetId = null, // FxTwitter doesn't provide this directly
            quoteTweetId = fxTweet.quote?.let { extractTweetIdFromUrl(it.url) },
            profileImageUrl = fxTweet.author.avatarUrl,
            language = fxTweet.lang,
            hashtags = extractHashtags(fxTweet.text),
            mentions = extractMentions(fxTweet.text),
            urls = extractUrls(fxTweet.text),
        )
    }

    private fun extractTweetIdFromUrl(url: String): String {
        // Extract ID from URLs like: https://twitter.com/username/status/1234567890
        val statusIndex = url.lastIndexOf("/status/")
        return if (statusIndex != -1) {
            val idStart = statusIndex + "/status/".length
            val idEnd = url.indexOf('?', idStart).takeIf { it != -1 } ?: url.length
            url.substring(idStart, idEnd)
        } else {
            // Fallback
            url.hashCode().toString()
        }
    }

    private val twitterDateFormatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH)

    private fun parseCreatedAt(createdAt: String): LocalDateTime {
        return try {
            val zonedDateTime = ZonedDateTime.parse(createdAt, twitterDateFormatter)
            zonedDateTime.toLocalDateTime()
        } catch (_: DateTimeParseException) {
            LocalDateTime.now()
        }
    }

    private fun extractMediaUrls(fxTweet: FxTweet): List<String> {
        val mediaUrls = mutableListOf<String>()

        fxTweet.media?.photos?.forEach { photo ->
            mediaUrls.add(photo.url)
        }

        fxTweet.media?.mosaic?.let { mosaic ->
            mediaUrls.add(mosaic.formats.jpeg)
        }

        return mediaUrls
    }

    private fun extractVideoUrls(fxTweet: FxTweet): List<String> {
        val videoUrls = mutableListOf<String>()

        fxTweet.media?.videos?.forEach { video ->
            videoUrls.add(video.url)
        }

        fxTweet.media?.external?.let { external ->
            if (external.type == "video") {
                videoUrls.add(external.url)
            }
        }

        return videoUrls
    }

    private fun extractHashtags(text: String): List<String> {
        val hashtagPattern = Regex("#([a-zA-Z0-9_]+)")
        return hashtagPattern.findAll(text)
            .map { it.groupValues[1] }
            .toList()
    }

    private fun extractMentions(text: String): List<String> {
        val mentionPattern = Regex("@([a-zA-Z0-9_]+)")
        return mentionPattern.findAll(text)
            .map { it.groupValues[1] }
            .toList()
    }

    private fun extractUrls(text: String): List<String> {
        val urlPattern = Regex("https?://\\S+")
        return urlPattern.findAll(text)
            .map { it.value }
            .toList()
    }
}
