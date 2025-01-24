package com.programistich.twitterx.telegram

import com.programistich.twitterx.telegram.models.TelegramConfig
import kotlinx.coroutines.future.await
import org.springframework.stereotype.Component
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery
import org.telegram.telegrambots.meta.api.methods.GetFile
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands
import org.telegram.telegrambots.meta.api.methods.description.SetMyDescription
import org.telegram.telegrambots.meta.api.methods.description.SetMyShortDescription
import org.telegram.telegrambots.meta.api.methods.name.SetMyName
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.methods.send.SendVideo
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.File
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.api.objects.polls.input.InputPollOption
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.meta.exceptions.TelegramApiValidationException
import java.io.Serializable

@Component
class TelegramSender(telegramConfig: TelegramConfig) {
    enum class ChatAction(val value: String) {
        TYPING("typing"),
        UPLOAD_PHOTO("upload_photo"),
        UPLOAD_VIDEO("upload_video")
    }

    private val telegramClient = OkHttpTelegramClient(telegramConfig.botToken)

    @Throws(TelegramApiException::class, TelegramApiValidationException::class)
    suspend fun sendText(
        text: String,
        chatId: String,
        customize: SendMessage.() -> Unit = {}
    ): Message {
        val method = SendMessage(chatId, text)
            .apply(customize)
            .apply { parseMode = ParseMode.HTML }
            .also { it.validate() }
        return telegramClient.executeAsync(method).await()
    }

    @Throws(TelegramApiException::class, TelegramApiValidationException::class)
    suspend fun editText(
        text: String,
        chatId: String,
        messageId: Int,
        customize: EditMessageText.() -> Unit = {}
    ): Serializable? {
        val method = EditMessageText(text).apply {
            this.chatId = chatId
            this.messageId = messageId
        }
            .apply(customize)
            .apply { parseMode = ParseMode.HTML }
            .also { it.validate() }

        return telegramClient.executeAsync(method).await()
    }

    @Throws(TelegramApiException::class, TelegramApiValidationException::class)
    suspend fun sendPhoto(
        imageUrl: String,
        chatId: String,
        customize: SendPhoto.() -> Unit = {}
    ): Message {
        val inputFile = InputFile(imageUrl)
        val method = SendPhoto(chatId, inputFile)
            .apply(customize)
            .apply { parseMode = ParseMode.HTML }
            .also { it.validate() }
        return telegramClient.executeAsync(method).await()
    }

    @Throws(TelegramApiException::class, TelegramApiValidationException::class)
    suspend fun sendVideo(
        videoUrl: String,
        chatId: String,
        customize: SendVideo.() -> Unit = {}
    ): Message {
        val inputFile = InputFile(videoUrl)
        val method = SendVideo(chatId, inputFile)
            .apply(customize)
            .apply { parseMode = ParseMode.HTML }
            .also { it.validate() }
        return telegramClient.executeAsync(method).await()
    }

    @Throws(TelegramApiException::class, TelegramApiValidationException::class)
    suspend fun sendVideo(
        video: java.io.File,
        chatId: String,
        customize: SendVideo.() -> Unit = {}
    ): Message {
        val inputFile = InputFile(video)
        val method = SendVideo(chatId, inputFile)
            .apply(customize)
            .apply { parseMode = ParseMode.HTML }
            .also { it.validate() }
        return telegramClient.executeAsync(method).await()
    }

    @Throws(TelegramApiException::class, TelegramApiValidationException::class)
    suspend fun sendMedias(
        urls: List<String>,
        chatId: String,
        text: String,
        parseMode: String = ParseMode.HTML,
        customize: SendMediaGroup.() -> Unit = {}
    ): List<Message> {
        val medias = urls.mapIndexed { index, url ->
            val media = InputMediaPhoto(url)
            if (index == 0) {
                media.caption = text
                media.parseMode = parseMode
            }
            media
        }

        val method = SendMediaGroup(chatId, medias).apply(customize).also { it.validate() }
        return telegramClient.executeAsync(method).await()
    }

    @Throws(TelegramApiException::class, TelegramApiValidationException::class)
    suspend fun sendPoll(
        chatId: String,
        text: String,
        options: List<String>,
        customize: SendPoll.() -> Unit = {}
    ): Message {
        val options = options.map { InputPollOption(it) }
        val method = SendPoll(chatId, text, options)
            .apply(customize)
            .also { it.validate() }
        return telegramClient.executeAsync(method).await()
    }

    @Throws(TelegramApiException::class, TelegramApiValidationException::class)
    suspend fun sendAction(
        chatId: String,
        action: ChatAction,
        customize: SendChatAction.() -> Unit = {}
    ): Boolean {
        val method = SendChatAction(chatId, action.value).apply(customize).also { it.validate() }
        return telegramClient.executeAsync(method).await()
    }

    @Throws(TelegramApiException::class, TelegramApiValidationException::class)
    suspend fun setMyCommands(
        commands: List<BotCommand>,
        customize: SetMyCommands.() -> Unit = {}
    ): Boolean {
        val method = SetMyCommands(commands).apply(customize).also { it.validate() }
        return telegramClient.executeAsync(method).await()
    }

    @Throws(TelegramApiException::class, TelegramApiValidationException::class)
    suspend fun setMyDescription(
        description: String,
        languageCode: String,
        customize: SetMyDescription.() -> Unit = {}
    ): Boolean {
        val method = SetMyDescription(description, languageCode).apply(customize).also { it.validate() }
        return telegramClient.executeAsync(method).await()
    }

    @Throws(TelegramApiException::class, TelegramApiValidationException::class)
    suspend fun setMyShortDescription(
        description: String,
        languageCode: String,
        customize: SetMyShortDescription.() -> Unit = {}
    ): Boolean {
        val method = SetMyShortDescription(description, languageCode).apply(customize).also { it.validate() }
        return telegramClient.executeAsync(method).await()
    }

    @Throws(TelegramApiException::class, TelegramApiValidationException::class)
    suspend fun setMyName(
        name: String,
        languageCode: String,
        customize: SetMyName.() -> Unit = {}
    ): Boolean {
        val method = SetMyName(name, languageCode).apply(customize).also { it.validate() }
        return telegramClient.executeAsync(method).await()
    }

    @Throws(TelegramApiException::class, TelegramApiValidationException::class)
    suspend fun deleteMessage(
        chatId: String,
        messageId: Int
    ): Boolean {
        val method = DeleteMessage(chatId, messageId).also { it.validate() }
        return telegramClient.executeAsync(method).await()
    }

    @Throws(TelegramApiException::class, TelegramApiValidationException::class)
    suspend fun sendInlineQuery(
        id: String,
        results: List<InlineQueryResult>,
        customize: AnswerInlineQuery.() -> Unit = {}
    ): Boolean {
        val method = AnswerInlineQuery(id, results).apply(customize).also { it.validate() }
        return telegramClient.executeAsync(method).await()
    }

    suspend fun getFile(fileId: String): File {
        val method = GetFile(fileId)
        return telegramClient.executeAsync(method).await()
    }

    fun downloadFile(filePath: String?): java.io.File {
        return telegramClient.downloadFile(filePath)
    }
}
