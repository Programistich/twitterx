package com.programistich.twitterx.telegram

import com.programistich.twitterx.features.dictionary.Dictionary
import com.programistich.twitterx.telegram.models.TelegramContext
import com.programistich.twitterx.telegram.models.TelegramUpdate
import com.programistich.twitterx.twitter.api.ApiFailTweetException
import com.programistich.twitterx.twitter.api.NotFoundTweetException
import com.programistich.twitterx.twitter.api.PrivateTweetException
import com.programistich.twitterx.twitter.service.Tweet
import com.programistich.twitterx.twitter.service.TweetContent
import org.springframework.stereotype.Component

@Component
class TelegramSenderTweet(
    private val dictionary: Dictionary,
    private val telegramSender: TelegramSender
) {
    suspend fun sendTweet(tweet: Result<Tweet>, context: TelegramContext) {
        tweet
            .onFailure { processError(context, it) }
            .onSuccess { processSuccess(context, it) }
    }

    private suspend fun processSuccess(context: TelegramContext, tweet: Tweet) {
        val chat = context.chat ?: return
        val update = context.update as? TelegramUpdate.Message ?: return

        when (tweet.content) {
            is TweetContent.ManyMedia -> {
                telegramSender.sendMedias(
                    chatId = chat.idStr(),
                    urls = tweet.content.urls,
                    text = tweet.text
                ) {
                    replyToMessageId = update.message.messageId
                }
            }
            is TweetContent.Photo -> {
                telegramSender.sendPhoto(tweet.content.url, chatId = chat.idStr()) {
                    caption = tweet.text
                    replyToMessageId = update.message.messageId
                }
            }
            is TweetContent.Poll -> {
                telegramSender.sendPoll(
                    chatId = chat.idStr(),
                    text = tweet.text,
                    options = tweet.content.options
                ) {
                    replyToMessageId = update.message.messageId
                }
            }
            TweetContent.Text -> {
                telegramSender.sendText(tweet.text, chatId = chat.idStr()) {
                    replyToMessageId = update.message.messageId
                }
            }
            is TweetContent.Video -> {
                telegramSender.sendVideo(tweet.content.url, chatId = chat.idStr()) {
                    caption = tweet.text
                    replyToMessageId = update.message.messageId
                }
            }
        }
    }

    private suspend fun processError(context: TelegramContext, exception: Throwable) {
        val chat = context.chat ?: return
        val update = context.update as? TelegramUpdate.Message ?: return

        val textTable = when (exception) {
            is PrivateTweetException -> "tweet-private"
            is NotFoundTweetException -> "tweet-not-found"
            is ApiFailTweetException -> "tweet-api-fail"
            else -> "tweet-idk"
        }

        val text = dictionary.getByLang(textTable, chat.language)
        telegramSender.sendText(text, chatId = chat.idStr()) {
            replyToMessageId = update.message.messageId
        }
    }
}
