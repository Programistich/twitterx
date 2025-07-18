package twitter.app.features.twitter

import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import twitterx.article.api.ArticleService
import twitterx.localization.api.LocalizationService
import twitterx.localization.api.MessageKey
import twitterx.telegram.api.TelegramAction
import twitterx.telegram.api.TelegramClient
import twitterx.translation.api.Language
import twitterx.translation.api.TranslationService
import twitterx.twitter.api.Tweet
import twitterx.twitter.api.TweetsThread

@Component
public class TweetSender(
    private val telegramClient: TelegramClient,
    private val translationService: TranslationService,
    private val localizationService: LocalizationService,
    private val articleService: ArticleService
) {
    private val logger = LoggerFactory.getLogger(TweetSender::class.java)

    private companion object {
        private const val TELEGRAM_MESSAGE_MAX_LENGTH = 4096
        private const val TELEGRAM_CAPTION_MAX_LENGTH = 1024
    }

    private suspend fun sendTweet(
        tweet: Tweet,
        chatId: Long,
        header: String,
        content: String,
        replyToMessageId: Long?
    ): Long? {
        telegramClient.sendChatAction(chatId, TelegramAction.TYPING)

        val hasPhotos = tweet.mediaUrls.isNotEmpty()
        val hasVideos = tweet.videoUrls.isNotEmpty()

        val fullContent = "$header\n\n$content"
        val title = "Tweet"

        val text = when {
            (hasPhotos || hasVideos) && fullContent.length > TELEGRAM_CAPTION_MAX_LENGTH -> {
                val articleResult = articleService.createArticle(content, "Tweet")
                if (articleResult.isSuccess) {
                    val articleUrl = articleResult.getOrThrow()
                    "$header\n\n$articleUrl"
                } else {
                    logger.error("Failed to create article for long caption", articleResult.exceptionOrNull())
                    fullContent.take(TELEGRAM_CAPTION_MAX_LENGTH - 3) + "..."
                }
            }
            (hasPhotos || hasVideos) -> fullContent
            fullContent.length > TELEGRAM_MESSAGE_MAX_LENGTH -> {
                val articleResult = articleService.createArticle(content, title)
                if (articleResult.isSuccess) {
                    val articleUrl = articleResult.getOrThrow()
                    "$header\n\n$articleUrl"
                } else {
                    logger.error("Failed to create article for long message", articleResult.exceptionOrNull())
                    fullContent.take(TELEGRAM_MESSAGE_MAX_LENGTH - 3) + "..."
                }
            }
            else -> fullContent
        }

        return sendTweet(
            chatId = chatId,
            replyToMessageId = replyToMessageId,
            text = text,
            tweet = tweet
        )
    }

    private suspend fun sendTweet(
        chatId: Long,
        replyToMessageId: Long?,
        text: String,
        tweet: Tweet
    ): Long? {
        val hasPhotos = tweet.mediaUrls.isNotEmpty()
        val hasVideos = tweet.videoUrls.isNotEmpty()

        return when {
            hasPhotos && hasVideos -> {
                sendMediaGroupWithCaptionAndGetMessageId(
                    tweet = tweet,
                    chatId = chatId,
                    replyToMessageId = replyToMessageId,
                    message = text
                )
            }

            hasPhotos -> {
                sendPhotosWithCaptionAndGetMessageId(
                    mediaUrls = tweet.mediaUrls,
                    chatId = chatId,
                    replyToMessageId = replyToMessageId,
                    caption = text
                )
            }

            hasVideos -> {
                sendVideosWithCaptionAndGetMessageId(
                    videoUrls = tweet.videoUrls,
                    chatId = chatId,
                    replyToMessageId = replyToMessageId,
                    caption = text
                )
            }

            else -> {
                sendTextAndGetMessageId(
                    chatId = chatId,
                    message = text,
                    replyToMessageId = replyToMessageId
                )
            }
        }
    }

    public suspend fun sendTweetThread(
        thread: TweetsThread,
        chatId: Long,
        userLanguage: Language,
        replyToMessageId: Long? = null,
        from: String? = null
    ): Long? {
        return when (thread) {
            is TweetsThread.Single -> {
                sendSingleTweet(thread.tweet, chatId, userLanguage, replyToMessageId, from)
            }
            is TweetsThread.Reply -> {
                sendReplyThread(thread, chatId, userLanguage, replyToMessageId, from)
            }
            is TweetsThread.QuoteThread -> {
                sendQuoteThread(thread, chatId, userLanguage, replyToMessageId, from)
            }
            is TweetsThread.RetweetThread -> {
                sendRetweetThread(thread, chatId, userLanguage, replyToMessageId, from)
            }
        }
    }

    public suspend fun sendSingleTweet(
        tweet: Tweet,
        chatId: Long,
        userLanguage: Language,
        replyToMessageId: Long? = null,
        from: String? = null
    ): Long? {
        val translatedContent = translateTweetContent(tweet.content, userLanguage)
        val header = formatTweetHeader(tweet, userLanguage, from)

        telegramClient.sendChatAction(chatId, TelegramAction.TYPING)

        return sendTweet(
            tweet = tweet,
            chatId = chatId,
            header = header,
            content = translatedContent,
            replyToMessageId = replyToMessageId
        )
    }

    private suspend fun sendReplyThread(
        thread: TweetsThread.Reply,
        chatId: Long,
        userLanguage: Language,
        replyToMessageId: Long?,
        from: String? = null
    ): Long? {
        val allTweets = mutableListOf<Tweet>()
        thread.quotedTweet?.let { quotedTweet ->
            allTweets.add(quotedTweet)
        }
        allTweets += thread.replies.reversed() + thread.tweet

        var currentReplyToMessageId = replyToMessageId

        for ((index, tweet) in allTweets.withIndex()) {
            val isLastTweet = index == allTweets.size - 1
            val tweetFrom = if (isLastTweet) from else null
            currentReplyToMessageId = sendSingleTweet(tweet, chatId, userLanguage, currentReplyToMessageId, tweetFrom)
        }

        return currentReplyToMessageId
    }

    private suspend fun sendQuoteThread(
        thread: TweetsThread.QuoteThread,
        chatId: Long,
        userLanguage: Language,
        replyToMessageId: Long?,
        from: String? = null
    ): Long? {
        val originalMessageId = sendSingleTweet(thread.original, chatId, userLanguage, replyToMessageId)
        return sendSingleTweet(thread.tweet, chatId, userLanguage, originalMessageId, from)
    }

    private suspend fun sendRetweetThread(
        thread: TweetsThread.RetweetThread,
        chatId: Long,
        userLanguage: Language,
        replyToMessageId: Long?,
        from: String? = null
    ): Long? {
        val translatedContent = translateTweetContent(thread.tweet.content, userLanguage)

        var header = localizationService.getMessage(
            MessageKey.TWEET_RETWEET_BY,
            userLanguage,
            mapOf(
                "retweeterUrl" to thread.whoRetweeted.url,
                "retweeter" to thread.whoRetweeted.username.removePrefix("@"),
                "username" to thread.tweet.username.removePrefix("@"),
                "userUrl" to "https://twitter.com/${thread.tweet.username.removePrefix("@")}",
                "tweetUrl" to thread.tweet.tweetUrl
            )
        )

        if (from != null) {
            header += " by $from"
        }

        return sendTweet(
            tweet = thread.tweet,
            chatId = chatId,
            header = header,
            content = translatedContent,
            replyToMessageId = replyToMessageId
        )
    }

    private suspend fun translateTweetContent(content: String, userLanguage: Language): String {
        if (content.isEmpty()) return ""
        val translationResult = translationService.translate(content, userLanguage)

        return if (translationResult.isSuccess) {
            val translation = translationResult.getOrThrow()
            if (translation.isSameLanguage()) {
                content
            } else {
                "[${translation.to.iso.uppercase()}] ${translation.text}\n\n[${translation.from.uppercase()}] $content"
            }
        } else {
            logger.warn("Failed to translate tweet content", translationResult.exceptionOrNull())
            content
        }
    }

    private suspend fun formatTweetHeader(
        tweet: Tweet,
        userLanguage: Language,
        from: String? = null
    ): String {
        val parameters = mapOf(
            "username" to tweet.username.removePrefix("@"),
            "fullName" to tweet.fullName,
            "url" to tweet.tweetUrl,
            "userUrl" to tweet.account.url
        )
        val header = localizationService.getMessage(MessageKey.TWEET_FROM, userLanguage, parameters)
        return if (from != null) {
            "$header by $from"
        } else {
            header
        }
    }

    private suspend fun sendTextAndGetMessageId(
        chatId: Long,
        message: String,
        replyToMessageId: Long?
    ): Long? {
        delay(1000) // 1 second delay before sending
        val result = telegramClient.sendMessage(
            chatId,
            message,
            "HTML",
            replyToMessageId = replyToMessageId,
            disableWebPagePreview = true
        )
        return if (result.isFailure) {
            logger.error("Failed to send tweet to chat $chatId", result.exceptionOrNull())
            null
        } else {
            logger.info("Successfully sent tweet to chat $chatId")
            result.getOrNull()?.messageId
        }
    }

    private suspend fun sendMediaGroupWithCaptionAndGetMessageId(
        tweet: Tweet,
        chatId: Long,
        replyToMessageId: Long?,
        message: String
    ): Long? {
        val mediaUrls = tweet.mediaUrls + tweet.videoUrls
        if (mediaUrls.isEmpty()) {
            logger.warn("No media URLs found for tweet ${tweet.id}")
            return null
        }

        telegramClient.sendChatAction(chatId, TelegramAction.UPLOAD_PHOTO)
        delay(1000) // 1 second delay before sending
        val result = telegramClient.sendMediaGroup(
            chatId,
            tweet.mediaUrls,
            tweet.videoUrls,
            message,
            replyToMessageId = replyToMessageId,
            "HTML"
        )
        return if (result.isFailure) {
            logger.error("Failed to send media group to chat $chatId", result.exceptionOrNull())
            null
        } else {
            logger.info("Successfully sent media group with ${mediaUrls.size} items to chat $chatId")
            result.getOrNull()?.firstOrNull()?.messageId
        }
    }

    private suspend fun sendPhotosWithCaptionAndGetMessageId(
        mediaUrls: List<String>,
        chatId: Long,
        replyToMessageId: Long?,
        caption: String
    ): Long? {
        if (mediaUrls.size == 1) {
            telegramClient.sendChatAction(chatId, TelegramAction.UPLOAD_PHOTO)
            delay(1000) // 1 second delay before sending
            val result = telegramClient.sendPhoto(
                chatId,
                mediaUrls.first(),
                caption,
                replyToMessageId = replyToMessageId,
                parseMode = "HTML"
            )
            return if (result.isFailure) {
                logger.error("Failed to send photo to chat $chatId", result.exceptionOrNull())
                null
            } else {
                logger.info("Successfully sent photo with caption to chat $chatId")
                result.getOrNull()?.messageId
            }
        } else {
            telegramClient.sendChatAction(chatId, TelegramAction.UPLOAD_PHOTO)
            delay(1000) // 1 second delay before sending
            val result = telegramClient.sendMediaGroup(
                chatId,
                mediaUrls,
                listOf(),
                caption,
                replyToMessageId = replyToMessageId,
                "HTML"
            )
            return if (result.isFailure) {
                logger.error("Failed to send media group to chat $chatId", result.exceptionOrNull())
                null
            } else {
                logger.info("Successfully sent ${mediaUrls.size} photos with caption to chat $chatId")
                result.getOrNull()?.firstOrNull()?.messageId
            }
        }
    }

    private suspend fun sendVideosWithCaptionAndGetMessageId(
        videoUrls: List<String>,
        chatId: Long,
        replyToMessageId: Long?,
        caption: String?
    ): Long? {
        if (videoUrls.size == 1) {
            telegramClient.sendChatAction(chatId, TelegramAction.UPLOAD_VIDEO)
            delay(1000) // 1 second delay before sending
            val result = telegramClient.sendVideo(
                chatId,
                videoUrls.first(),
                caption,
                replyToMessageId = replyToMessageId,
                parseMode = "HTML"
            )
            return if (result.isFailure) {
                logger.error("Failed to send video to chat $chatId", result.exceptionOrNull())
                null
            } else {
                logger.info("Successfully sent video with caption to chat $chatId")
                result.getOrNull()?.messageId
            }
        } else {
            telegramClient.sendChatAction(chatId, TelegramAction.UPLOAD_VIDEO)
            delay(1000) // 1 second delay before sending
            val result = telegramClient.sendMediaGroup(
                chatId,
                listOf(),
                videoUrls,
                caption,
                replyToMessageId = replyToMessageId,
                "HTML"
            )
            return if (result.isFailure) {
                logger.error("Failed to send video to chat $chatId", result.exceptionOrNull())
                null
            } else {
                logger.info("Successfully sent video${if (caption != null) " with caption" else ""} to chat $chatId")
                result.getOrNull()?.firstOrNull()?.messageId
            }
        }
    }
}
