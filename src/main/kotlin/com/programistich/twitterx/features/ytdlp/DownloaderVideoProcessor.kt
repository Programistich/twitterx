package com.programistich.twitterx.features.ytdlp

import com.programistich.twitterx.entities.TelegramChat
import com.programistich.twitterx.features.dictionary.Dictionary
import com.programistich.twitterx.telegram.TelegramSender
import com.programistich.twitterx.telegram.getUrls
import com.programistich.twitterx.telegram.models.TelegramConfig
import com.programistich.twitterx.telegram.models.TelegramContext
import com.programistich.twitterx.telegram.models.TelegramUpdate
import com.programistich.twitterx.telegram.processor.TelegramProcessor
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class DownloaderVideoProcessor(
    private val ytDlpFacade: YtDlpFacade,
    private val telegramSender: TelegramSender,
    private val dictionary: Dictionary,
    private val telegramConfig: TelegramConfig
) : TelegramProcessor {
    private val logger = LoggerFactory.getLogger(javaClass::class.java)

    override val priority: TelegramProcessor.Priority
        get() = TelegramProcessor.Priority.HIGH

    override suspend fun canProcess(context: TelegramContext): Boolean {
        val update = context.update as? TelegramUpdate.Message ?: return false
        return update.message.getUrls().any { isIG(it) || isTikTok(it) }
    }

    override suspend fun process(context: TelegramContext) {
        val update = context.update as? TelegramUpdate.Message ?: return
        val messageId = update.message.messageId
        val chat = context.chat ?: return

        update
            .message
            .getUrls()
            .filter { isIG(it) || isTikTok(it) }
            .forEach { processUrl(it, chat, messageId) }
    }

    private suspend fun processUrl(
        url: String,
        chat: TelegramChat,
        messageId: Int
    ) {
        val chatId = chat.idStr()

        try {
            telegramSender.sendAction(chatId, TelegramSender.ChatAction.UPLOAD_VIDEO)

            val video = ytDlpFacade.download(url)
            telegramSender.sendVideo(video = video, chatId = chatId) {
                replyToMessageId = messageId
                disableNotification()
            }
        } catch (e: UnAuthorizedException) {
            val text = dictionary.getByLang("yt-dlp-cookies-expired", chat.language)
            telegramSender.sendText(text, chatId)
            telegramSender.sendText("Need to update cookies", telegramConfig.ownerId)
        } catch (e: RateLimitException) {
            val text = dictionary.getByLang("yt-dlp-rate-limit", chat.language)
            telegramSender.sendText(text, chatId)
        } catch (e: Exception) {
            val text = dictionary.getByLang("yt-dlp-download-failed", chat.language)
            telegramSender.sendText(text, chatId)
            logger.error("Failed to download video from $url", e)
        }
    }

    private fun isTikTok(text: String) = text.contains("tiktok", ignoreCase = true)

    private fun isIG(text: String) = text.contains("instagram.com/reel", ignoreCase = true)
}
