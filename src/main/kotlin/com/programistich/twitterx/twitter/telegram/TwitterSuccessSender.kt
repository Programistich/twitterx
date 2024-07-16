package com.programistich.twitterx.twitter.telegram

import com.programistich.twitterx.entities.ChatLanguage
import com.programistich.twitterx.features.telegraph.TelegraphService
import com.programistich.twitterx.features.translate.TranslateService
import com.programistich.twitterx.telegram.TelegramSender
import com.programistich.twitterx.telegram.models.TelegramContext
import com.programistich.twitterx.telegram.models.TelegramUpdate
import com.programistich.twitterx.twitter.api.LongTweetException
import com.programistich.twitterx.twitter.service.Tweet
import com.programistich.twitterx.twitter.service.TweetContent
import kotlinx.coroutines.delay
import org.springframework.stereotype.Component

@Component
class TwitterSuccessSender(
    private val telegramSender: TelegramSender,
    private val translateService: TranslateService,
    private val telegraphService: TelegraphService
) {
    companion object {
        private const val WAIT_ACTION_TIME = 2000L
    }

    suspend fun sendTweet(
        context: TelegramContext,
        tweet: Tweet
    ) {
        val chat = context.chat ?: return
        val update = context.update as? TelegramUpdate.Message ?: return

        val chatId = chat.idStr()
        val messageId = update.message.messageId

        val translate = getTranslatedText(chat.language, tweet)
        val contentText = getText(translate, tweet)

        sendAction(chatId, tweet.content)
        when (tweet.content) {
            is TweetContent.ManyMedia -> {
                telegramSender.sendMedias(
                    urls = tweet.content.urls,
                    chatId = chatId,
                    text = contentText
                ) {
                    replyToMessageId = messageId
                }
            }
            is TweetContent.Photo -> {
                telegramSender.sendPhoto(
                    imageUrl = tweet.content.url,
                    chatId = chatId
                ) {
                    caption = contentText
                    replyToMessageId = messageId
                }
            }
            is TweetContent.Poll -> {
                telegramSender.sendPoll(
                    chatId = chat.idStr(),
                    text = contentText,
                    options = tweet.content.options
                ) {
                    replyToMessageId = messageId
                }
            }
            TweetContent.Text -> {
                telegramSender.sendText(
                    text = contentText,
                    chatId = chat.idStr()
                ) {
                    replyToMessageId = messageId
                }
            }
            is TweetContent.Video -> {
                telegramSender.sendVideo(
                    videoUrl = tweet.content.url,
                    chatId = chat.idStr()
                ) {
                    caption = contentText
                    replyToMessageId = messageId
                }
            }
        }
    }

    private suspend fun sendAction(chatId: String, content: TweetContent) {
        val action = when (content) {
            is TweetContent.ManyMedia -> TelegramSender.ChatAction.UPLOAD_VIDEO
            is TweetContent.Photo -> TelegramSender.ChatAction.UPLOAD_PHOTO
            is TweetContent.Poll -> TelegramSender.ChatAction.TYPING
            TweetContent.Text -> TelegramSender.ChatAction.TYPING
            is TweetContent.Video -> TelegramSender.ChatAction.UPLOAD_VIDEO
        }
        telegramSender.sendAction(chatId, action)
        delay(WAIT_ACTION_TIME)
    }

    private fun getTranslatedText(
        language: ChatLanguage,
        tweet: Tweet
    ): String {
        val result = translateService.translate(tweet.text, language)

        val model = result.getOrNull() ?: return tweet.text
        if (model.from == language.iso) return tweet.text

        val toLang = "[${language.iso.uppercase()}]"
        val fromLang = "[${model.from.uppercase()}]"

        return "$fromLang ${tweet.text}\n\n$toLang ${model.text}"
    }

    private suspend fun getText(
        translatedText: String,
        tweet: Tweet,
    ): String {
        val limit = getLimit(tweet.content)
        if (translatedText.length <= limit) return translatedText
        else throw LongTweetException()
    }

    private fun getLimit(content: TweetContent): Int {
        return when (content) {
            is TweetContent.ManyMedia -> 1024
            is TweetContent.Photo -> 1024
            is TweetContent.Poll -> 300
            TweetContent.Text -> 4096
            is TweetContent.Video -> 1024
        }
    }
}
