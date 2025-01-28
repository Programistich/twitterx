package com.programistich.twitterx.features.ytdlp

import com.programistich.twitterx.core.executors.Executor
import com.programistich.twitterx.core.repos.TelegramChat
import com.programistich.twitterx.core.telegram.models.TelegramContext
import com.programistich.twitterx.core.telegram.updates.TelegramMessageUpdate
import kotlinx.coroutines.future.await
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.methods.send.SendVideo
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto
import org.telegram.telegrambots.meta.generics.TelegramClient

@Component
class DownloaderYtDlpProcessor(
    private val ytDlpFacade: YtDlpFacade,
    private val instagramBuilderURL: InstagramBuilderURL,
    private val telegramClient: TelegramClient,
    private val tikTokApi: TikTokApi
) : Executor<TelegramMessageUpdate> {
    private val logger = LoggerFactory.getLogger(javaClass::class.java)

    override val priority: Executor.Priority
        get() = Executor.Priority.HIGH

    override suspend fun canProcess(context: TelegramContext<TelegramMessageUpdate>): Boolean {
        return context.update.getUrls().any { isIG(it) || isTikTok(it) || isYoutubeShorts(it) }
    }

    override suspend fun process(context: TelegramContext<TelegramMessageUpdate>): Result<Unit> {
        val chat = context.chat ?: return Result.failure(IllegalArgumentException("Chat not found"))
        val messageId = context.update.messageId()

        context.update.getUrls()
            .filter { isIG(it) || isTikTok(it) || isYoutubeShorts(it) }
            .forEach { processUrl(it, chat, messageId) }

        return Result.success(Unit)
    }

    private suspend fun processUrl(
        url: String,
        chat: TelegramChat,
        messageId: Int
    ) {
        runCatching {
            val sendAction = SendChatAction(chat.idStr(), "upload_video")
            telegramClient.executeAsync(sendAction).await()
        }

        try {
            when {
                isTikTok(url) -> sendTikTok(url, chat, messageId)
                isIG(url) -> sendInstagram(url, chat, messageId)
                isYoutubeShorts(url) -> sendYoutubeShorts(url, chat, messageId)
            }
        } catch (e: Exception) {
            logger.error("Failed to download video from $url", e)
        }
    }

    private suspend fun sendYoutubeShorts(url: String, chat: TelegramChat, messageId: Int) {
        val file = ytDlpFacade.download(url)
        val inputFile = InputFile(file)
        val sendVideo = SendVideo(chat.idStr(), inputFile)
        sendVideo.replyToMessageId = messageId
        telegramClient.executeAsync(sendVideo).await()
    }

    private suspend fun sendInstagram(url: String, chat: TelegramChat, messageId: Int) {
        val url = instagramBuilderURL.downloadIG(url)
        val file = ytDlpFacade.download(url)
        val inputFile = InputFile(file)
        val sendVideo = SendVideo(chat.idStr(), inputFile)
        sendVideo.replyToMessageId = messageId
        telegramClient.executeAsync(sendVideo).await()
    }

    private suspend fun sendTikTok(url: String, chat: TelegramChat, messageId: Int) {
        when (val type = tikTokApi.getContent(url)) {
            is VideoResponse -> {
                val inputFile = InputFile(type.video)
                val sendVideo = SendVideo(chat.idStr(), inputFile)
                sendVideo.replyToMessageId = messageId
                telegramClient.executeAsync(sendVideo).await()
            }
            is ImagesResponse -> {
                for (photos in type.photo.chunked(10)) {
                    if (photos.size == 1) {
                        val media = InputFile(photos.first())
                        val sendPhoto = SendPhoto(chat.idStr(), media)
                        sendPhoto.replyToMessageId = messageId
                        telegramClient.executeAsync(sendPhoto).await()
                    } else {
                        val media = photos.map { InputMediaPhoto(it) }
                        val sendMediaGroup = SendMediaGroup(chat.idStr(), media)
                        sendMediaGroup.replyToMessageId = messageId
                        telegramClient.executeAsync(sendMediaGroup).await()
                    }
                }
            }
        }
    }

    private fun isTikTok(text: String) = text.contains("tiktok", ignoreCase = true)

    private fun isIG(text: String) = text.contains("instagram", ignoreCase = true)

    private fun isYoutubeShorts(text: String) = text.contains("youtube.com/shorts", ignoreCase = true)
}
