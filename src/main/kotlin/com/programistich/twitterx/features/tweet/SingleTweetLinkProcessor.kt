package com.programistich.twitterx.features.tweet

import com.programistich.twitterx.telegram.TelegramSenderTweet
import com.programistich.twitterx.telegram.models.TelegramContext
import com.programistich.twitterx.telegram.models.TelegramUpdate
import com.programistich.twitterx.telegram.processor.TelegramProcessor
import com.programistich.twitterx.twitter.service.TwitterService
import org.springframework.stereotype.Component

@Component
class SingleTweetLinkProcessor(
    private val twitterService: TwitterService,
    private val telegramSenderTweet: TelegramSenderTweet
) : TelegramProcessor {
    override val priority: TelegramProcessor.Priority
        get() = TelegramProcessor.Priority.MEDIUM

    override suspend fun canProcess(context: TelegramContext): Boolean {
        val update = context.update as? TelegramUpdate.Message ?: return false
        return getTweetIds(update.message.text).size == 1
    }

    override suspend fun process(context: TelegramContext) {
        val update = context.update as? TelegramUpdate.Message ?: return
        val tweetId = getTweetIds(update.message.text).firstOrNull() ?: return

        val tweet = twitterService.getTweet(tweetId)
        kotlin.runCatching {
            telegramSenderTweet.sendTweet(tweet, context)
        }.onFailure {
            println("Error while sending tweet: $tweetId $tweet")
            it.printStackTrace()
        }
    }

    private fun getTweetIds(text: String): List<String> {
        return TWEET_REGEX.toRegex().findAll(text).map { it.groupValues[2] }.toList()
    }

    companion object {
        private const val TWEET_REGEX = "https://(?:mobile.)?(?:twitter.com|x.com)/([a-zA-Z0-9_]+)/status/([0-9]+)?(.*)"
    }
}
