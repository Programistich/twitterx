package com.programistich.twitterx.features.cookies

import com.programistich.twitterx.telegram.TelegramSender
import com.programistich.twitterx.telegram.models.TelegramContext
import com.programistich.twitterx.telegram.models.TelegramUpdate
import com.programistich.twitterx.telegram.processor.BotOwnerProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component
import java.util.logging.Logger

@Component
class UploadCookiesExecutor(
    private val cookiesFacade: CookiesFacade,
    private val telegramSender: TelegramSender
) : BotOwnerProcessor() {

    private val logger = Logger.getLogger(this::class.java.name)

    override fun isCanExecuteInternal(context: TelegramContext): Boolean {
        val update = context.update as? TelegramUpdate.Message ?: return false

        val document = update.message.document ?: return false
        return document.fileName == CookiesFacade.FILE_NAME
    }

    override suspend fun process(context: TelegramContext) {
        val update = context.update as? TelegramUpdate.Message ?: return
        val chatId = update.message.chat.id
        val fileId = update.message.document.fileId

        try {
            val filePath = telegramSender.getFile(fileId).filePath

            val telegramFile = telegramSender.downloadFile(filePath)
            val cookiesFile = cookiesFacade.getCookiesFile()

            if (cookiesFile.exists().not()) {
                withContext(Dispatchers.IO) {
                    cookiesFile.createNewFile()
                }
            }

            telegramFile.copyTo(cookiesFile, true)
            telegramSender.sendText(text = "Cookies uploaded", chatId = chatId.toString())
        } catch (e: Exception) {
            logger.severe("Failed to upload cookies: ${e.message}")
            telegramSender.sendText(text = "Failed to upload cookies", chatId = chatId.toString())
        }
    }
}
