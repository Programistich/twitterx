package com.programistich.twitterx.features.tweet

import com.programistich.twitterx.features.dictionary.Dictionary
import com.programistich.twitterx.telegram.TelegramSender
import com.programistich.twitterx.telegram.models.TelegramContext
import com.programistich.twitterx.telegram.models.TelegramUpdate
import com.programistich.twitterx.twitter.api.ApiFailTweetException
import com.programistich.twitterx.twitter.api.LongTweetException
import com.programistich.twitterx.twitter.api.NotFoundTweetException
import com.programistich.twitterx.twitter.api.PrivateTweetException
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

        val exceptionText = getKeyTextByException(exception)
        val translatedExceptionText = dictionary.getByLang(exceptionText, chat.language)

        val text = if (isCanPostFxTwitter(exception)) {
            val fixUpxUrl = "https://fixupx.com/status/$tweetId"
            val tweetIdText = dictionary.getByLang("tweet-fixupx", chat.language, fixUpxUrl)
            "$translatedExceptionText\n\n$tweetIdText"
        } else {
            translatedExceptionText
        }

        telegramSender.sendText(text = text, chatId = chat.idStr()) {
            replyToMessageId = messageId
        }
    }

    private fun getKeyTextByException(exception: Throwable): String {
        return when (exception) {
            is PrivateTweetException -> "tweet-private"
            is NotFoundTweetException -> "tweet-not-found"
            is ApiFailTweetException -> "tweet-api-fail"
            is TelegramApiValidationException -> "tweet-telegram-validate-error"
            is TelegramApiException -> "tweet-telegram-error"
            is LongTweetException -> "tweet-long-error"
            else -> "tweet-unknown-error"
        }
    }

    private fun isCanPostFxTwitter(exception: Throwable): Boolean {
        return when (exception) {
            is PrivateTweetException -> false
            is NotFoundTweetException -> false
            is ApiFailTweetException -> false
            else -> true
        }
    }
}
