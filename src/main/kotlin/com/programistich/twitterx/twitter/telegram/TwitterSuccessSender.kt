package com.programistich.twitterx.twitter.telegram

import com.programistich.twitterx.telegram.TelegramSender
import com.programistich.twitterx.telegram.models.TelegramContext
import com.programistich.twitterx.telegram.models.TelegramUpdate
import com.programistich.twitterx.twitter.service.Tweet
import com.programistich.twitterx.twitter.service.TweetContent
import kotlinx.coroutines.delay
import org.springframework.stereotype.Component

@Component
class TwitterSuccessSender(
    private val telegramSender: TelegramSender
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

        sendAction(chatId, tweet.content)

        when (tweet.content) {
            is TweetContent.ManyMedia -> {
                telegramSender.sendMedias(
                    urls = tweet.content.urls,
                    chatId = chatId,
                    text = tweet.text
                ) {
                    replyToMessageId = messageId
                }
            }
            is TweetContent.Photo -> {
                telegramSender.sendPhoto(
                    imageUrl = tweet.content.url,
                    chatId = chatId
                ) {
                    caption = tweet.text
                    replyToMessageId = messageId
                }
            }
            is TweetContent.Poll -> {
                telegramSender.sendPoll(
                    chatId = chat.idStr(),
                    text = tweet.text,
                    options = tweet.content.options
                ) {
                    replyToMessageId = messageId
                }
            }
            TweetContent.Text -> {
                telegramSender.sendText(
                    text = tweet.text,
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
                    caption = tweet.text
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
}
