package com.programistich.twitterx.features.tweet

import com.programistich.twitterx.telegram.TelegramSender
import com.programistich.twitterx.telegram.models.TelegramContext
import com.programistich.twitterx.telegram.models.TelegramUpdate
import com.programistich.twitterx.telegram.processor.TelegramProcessor
import com.programistich.twitterx.twitter.service.TwitterService
import com.programistich.twitterx.twitter.telegram.TwitterErrorSender
import com.programistich.twitterx.twitter.telegram.TwitterSuccessSender
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class SingleTweetLinkProcessor(
    private val twitterService: TwitterService,
    private val twitterSuccessSender: TwitterSuccessSender,
    private val twitterErrorSender: TwitterErrorSender,
    private val telegramSender: TelegramSender
) : TelegramProcessor {
    companion object {
        private const val TWEET_REGEX = "https://(?:mobile.)?(?:twitter.com|x.com)/([a-zA-Z0-9_]+)/status/([0-9]+)?(.*)"
    }

    private val logger = LoggerFactory.getLogger(this::class.java)

    override val priority: TelegramProcessor.Priority
        get() = TelegramProcessor.Priority.MEDIUM

    override suspend fun canProcess(context: TelegramContext): Boolean {
        val update = context.update as? TelegramUpdate.Message ?: return false
        val text = update.message.text ?: return false
        return getTweetIds(text).size == 1
    }

    override suspend fun process(context: TelegramContext) {
        val update = context.update as? TelegramUpdate.Message ?: return
        val text = update.message.text ?: return

        val chat = context.chat ?: return
        val chatId = chat.idStr()
        telegramSender.sendAction(chatId, TelegramSender.ChatAction.TYPING)

        val tweetId = getTweetIds(text).firstOrNull() ?: return

        twitterService
            .getTweet(tweetId)
            .mapCatching { tweet ->
                twitterSuccessSender.sendTweet(context, tweet)
            }
            .onFailure { exception ->
                logger.error("Failed to send tweet", exception)
                twitterErrorSender.sendError(context, exception, tweetId)
            }
    }

    private fun getTweetIds(text: String): List<String> {
        return TWEET_REGEX.toRegex().findAll(text).map { it.groupValues[2] }.toList()
    }
}
