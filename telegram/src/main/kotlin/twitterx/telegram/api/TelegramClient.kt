package twitterx.telegram.api

import twitterx.telegram.api.models.inline.InlineQueryResult
import twitterx.telegram.api.models.keyboard.InlineKeyboardMarkup
import twitterx.telegram.api.models.response.TelegramMessage
import java.io.File

public interface TelegramClient {
    public suspend fun sendMessage(
        chatId: Long,
        text: String,
        parseMode: String? = null,
        replyMarkup: InlineKeyboardMarkup? = null,
        replyToMessageId: Long? = null,
        disableWebPagePreview: Boolean = false
    ): Result<TelegramMessage>

    public suspend fun sendPhoto(
        chatId: Long,
        photoUrl: String,
        caption: String? = null,
        parseMode: String? = null,
        replyToMessageId: Long?,
    ): Result<TelegramMessage>

    public suspend fun sendVideo(
        chatId: Long,
        videoUrl: String,
        caption: String? = null,
        parseMode: String? = null,
        replyToMessageId: Long?,
    ): Result<TelegramMessage>

    public suspend fun sendVideo(
        chatId: Long,
        videoPath: File,
        caption: String? = null,
        parseMode: String? = null,
        replyToMessageId: Long?
    ): Result<TelegramMessage>

    public suspend fun sendMediaGroup(
        chatId: Long,
        photoUrls: List<String>,
        videoUrls: List<String>,
        caption: String? = null,
        replyToMessageId: Long?,
        parseMode: String? = null
    ): Result<List<TelegramMessage>>

    public suspend fun editMessage(
        chatId: Long,
        messageId: Long,
        text: String,
        parseMode: String? = null,
        replyMarkup: InlineKeyboardMarkup? = null
    ): Result<Unit>

    public suspend fun deleteMessage(
        chatId: Long,
        messageId: Long
    ): Result<Boolean>

    public suspend fun sendChatAction(
        chatId: Long,
        action: TelegramAction
    ): Result<Boolean>

    public suspend fun answerInlineQuery(
        inlineQueryId: String,
        results: List<InlineQueryResult>,
        cacheTime: Int? = null,
        isPersonal: Boolean? = null,
        nextOffset: String? = null
    ): Result<Boolean>
}

public enum class TelegramAction(public val value: String) {
    TYPING("typing"),
    UPLOAD_PHOTO("upload_photo"),
    UPLOAD_VIDEO("upload_video"),
    UPLOAD_DOCUMENT("upload_document")
}
