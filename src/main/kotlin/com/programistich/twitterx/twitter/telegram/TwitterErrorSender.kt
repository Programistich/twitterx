package com.programistich.twitterx.twitter.telegram

import com.programistich.twitterx.features.dictionary.Dictionary
import com.programistich.twitterx.telegram.TelegramSender
import com.programistich.twitterx.telegram.models.TelegramContext
import com.programistich.twitterx.telegram.models.TelegramUpdate
import com.programistich.twitterx.twitter.api.ApiFailTweetException
import com.programistich.twitterx.twitter.api.NotFoundTweetException
import com.programistich.twitterx.twitter.api.PrivateTweetException
import com.programistich.twitterx.twitter.api.TweetException
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.meta.exceptions.TelegramApiValidationException

@Component
class TwitterErrorSender(
    private val telegramSender: TelegramSender,
    private val dictionary: Dictionary
) {
    suspend fun sendError(
        context: TelegramContext,
        exception: Throwable,
        tweetId: String
    ) {
        val chat = context.chat ?: return
        val update = context.update as? TelegramUpdate.Message ?: return
        val messageId = update.message.messageId

        val exceptionText = when (exception) {
            is PrivateTweetException -> "tweet-private"
            is NotFoundTweetException -> "tweet-not-found"
            is ApiFailTweetException -> "tweet-api-fail"
            is TelegramApiValidationException -> "tweet-telegram-validate-error"
            is TelegramApiException -> "tweet-telegram-error"
            else -> "tweet-unknown-error"
        }
        val translatedExceptionText = dictionary.getByLang(exceptionText, chat.language)

        val text = if (exception is TweetException) {
            translatedExceptionText
        } else {
            val fixUpxUrl = "https://fixupx.com/status/$tweetId"
            val tweetIdText = dictionary.getByLang("tweet-fixupx", chat.language, fixUpxUrl)
            "$translatedExceptionText\n\n$tweetIdText"
        }

        telegramSender.sendText(
            text = text,
            chatId = chat.idStr()
        ) {
            replyToMessageId = messageId
        }
    }
}
