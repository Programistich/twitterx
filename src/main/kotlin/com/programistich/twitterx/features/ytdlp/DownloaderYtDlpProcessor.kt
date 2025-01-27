package com.programistich.twitterx.features.ytdlp

import com.programistich.twitterx.core.executors.Executor
import com.programistich.twitterx.core.repos.TelegramChat
import com.programistich.twitterx.core.telegram.models.TelegramContext
import com.programistich.twitterx.core.telegram.updates.TelegramMessageUpdate
import kotlinx.coroutines.future.await
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction
import org.telegram.telegrambots.meta.api.methods.send.SendVideo
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.generics.TelegramClient

@Component
class DownloaderYtDlpProcessor(
    private val ytDlpFacade: YtDlpFacade,
    private val instagramBuilderURL: InstagramBuilderURL,
    private val telegramClient: TelegramClient
) : Executor<TelegramMessageUpdate> {
    private val logger = LoggerFactory.getLogger(javaClass::class.java)

    override val priority: Executor.Priority
        get() = Executor.Priority.HIGH

    override suspend fun canProcess(context: TelegramContext<TelegramMessageUpdate>): Boolean {
        return context.update.getUrls().any { isIG(it) || isTikTok(it) }
    }

    override suspend fun process(context: TelegramContext<TelegramMessageUpdate>): Result<Unit> {
        val chat = context.chat ?: return Result.failure(IllegalArgumentException("Chat not found"))
        val messageId = context.update.messageId()

        context.update.getUrls()
            .filter { isIG(it) || isTikTok(it) }
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

        val url = if (isIG(url)) instagramBuilderURL.downloadIG(url) else url

        try {
            val file = ytDlpFacade.download(url)

            val inputFile = InputFile(file)
            val sendVideo = SendVideo(chat.idStr(), inputFile)

            telegramClient.executeAsync(sendVideo).await()
        } catch (e: Exception) {
            logger.error("Failed to download video from $url", e)
        }
    }

    private fun isTikTok(text: String) = text.contains("tiktok", ignoreCase = true)

    private fun isIG(text: String) = text.contains("instagram", ignoreCase = true)
}
