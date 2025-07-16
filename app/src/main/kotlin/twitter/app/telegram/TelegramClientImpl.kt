package twitter.app.telegram

import kotlinx.coroutines.future.await
import org.slf4j.LoggerFactory
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.methods.send.SendVideo
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultPhoto
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto
import org.telegram.telegrambots.meta.api.objects.media.InputMediaVideo
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow
import twitterx.telegram.api.TelegramAction
import twitterx.telegram.api.TelegramClient
import twitterx.telegram.api.models.inline.InlineQueryResult
import twitterx.telegram.api.models.keyboard.InlineKeyboardMarkup
import twitterx.telegram.api.models.response.TelegramChat
import twitterx.telegram.api.models.response.TelegramMessage
import java.io.File

public class TelegramClientImpl(
    botToken: String
) : TelegramClient {

    private val telegramClient = OkHttpTelegramClient(botToken)
    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun sendMessage(
        chatId: Long,
        text: String,
        parseMode: String?,
        replyMarkup: InlineKeyboardMarkup?,
        replyToMessageId: Long?,
        disableWebPagePreview: Boolean
    ): Result<TelegramMessage> = runCatching {
        val sendMessage = SendMessage(chatId.toString(), text).apply {
            this.parseMode = parseMode
            this.replyMarkup = replyMarkup?.toTelegramMarkup()
            this.replyToMessageId = replyToMessageId?.toInt()
            this.disableWebPagePreview = disableWebPagePreview
        }

        val message = telegramClient.executeAsync(sendMessage).await()
        TelegramMessage(
            messageId = message.messageId.toLong(),
            chat = TelegramChat(message.chat.id, message.chat.type),
            text = message.text
        )
    }.onFailure { error ->
        logger.error("Failed to send message to chat $chatId", error)
    }

    override suspend fun sendPhoto(
        chatId: Long,
        photoUrl: String,
        caption: String?,
        parseMode: String?,
        replyToMessageId: Long?
    ): Result<TelegramMessage> = runCatching {
        val sendPhoto = SendPhoto(chatId.toString(), InputFile(photoUrl)).apply {
            this.caption = caption
            this.parseMode = parseMode
            this.replyToMessageId = replyToMessageId?.toInt()
        }

        val message = telegramClient.executeAsync(sendPhoto).await()
        TelegramMessage(
            messageId = message.messageId.toLong(),
            chat = TelegramChat(message.chat.id, message.chat.type),
            caption = message.caption
        )
    }.onFailure { error ->
        logger.error("Failed to send photo to chat $chatId", error)
    }

    override suspend fun sendVideo(
        chatId: Long,
        videoUrl: String,
        caption: String?,
        parseMode: String?,
        replyToMessageId: Long?
    ): Result<TelegramMessage> = runCatching {
        val sendVideo = SendVideo(chatId.toString(), InputFile(videoUrl)).apply {
            this.caption = caption
            this.parseMode = parseMode
            this.replyToMessageId = replyToMessageId?.toInt()
        }

        val message = telegramClient.executeAsync(sendVideo).await()
        TelegramMessage(
            messageId = message.messageId.toLong(),
            chat = TelegramChat(message.chat.id, message.chat.type),
            caption = message.caption
        )
    }.onFailure { error ->
        logger.error("Failed to send video to chat $chatId", error)
    }

    override suspend fun sendVideo(
        chatId: Long,
        videoPath: File,
        caption: String?,
        parseMode: String?,
        replyToMessageId: Long?
    ): Result<TelegramMessage> = runCatching {
        val sendVideo = SendVideo(chatId.toString(), InputFile(videoPath)).apply {
            this.caption = caption
            this.parseMode = parseMode
            this.replyToMessageId = replyToMessageId?.toInt()
        }

        val message = telegramClient.executeAsync(sendVideo).await()
        TelegramMessage(
            messageId = message.messageId.toLong(),
            chat = TelegramChat(message.chat.id, message.chat.type),
            caption = message.caption
        )
    }.onFailure { error ->
        logger.error("Failed to send video to chat $chatId", error)
    }

    override suspend fun sendMediaGroup(
        chatId: Long,
        photoUrls: List<String>,
        videoUrls: List<String>,
        caption: String?,
        replyToMessageId: Long?,
        parseMode: String?
    ): Result<List<TelegramMessage>> = runCatching {
        val photoMediaGroup = photoUrls.mapIndexed { index, url ->
            InputMediaPhoto(url)
        }

        val videoMediaGroup = videoUrls.mapIndexed { index, url ->
            InputMediaVideo(url)
        }

        val mediaGroup = (photoMediaGroup + videoMediaGroup).toMutableList()
        mediaGroup[0] = mediaGroup[0].apply {
            if (caption != null) {
                this.caption = caption
                this.parseMode = parseMode
            }
        }

        val sendMediaGroup = SendMediaGroup(chatId.toString(), mediaGroup).apply {
            this.replyToMessageId = replyToMessageId?.toInt()
        }
        val messages = telegramClient.executeAsync(sendMediaGroup).await()

        messages.map { message ->
            TelegramMessage(
                messageId = message.messageId.toLong(),
                chat = TelegramChat(message.chat.id, message.chat.type),
                caption = message.caption
            )
        }
    }.onFailure { error ->
        logger.error("Failed to send media group to chat $chatId", error)
    }

    override suspend fun editMessage(
        chatId: Long,
        messageId: Long,
        text: String,
        parseMode: String?,
        replyMarkup: InlineKeyboardMarkup?
    ): Result<Unit> = runCatching {
        val editMessage = EditMessageText(text).apply {
            this.chatId = chatId.toString()
            this.messageId = messageId.toInt()
            this.parseMode = parseMode
            this.replyMarkup = replyMarkup?.toTelegramMarkup()
        }
        telegramClient.executeAsync(editMessage).await()
        Unit
    }.onFailure { error ->
        logger.error("Failed to edit message $messageId in chat $chatId", error)
    }

    override suspend fun deleteMessage(
        chatId: Long,
        messageId: Long
    ): Result<Boolean> = runCatching {
        val deleteMessage = DeleteMessage(chatId.toString(), messageId.toInt())
        telegramClient.executeAsync(deleteMessage).await()
    }.onFailure { error ->
        logger.error("Failed to delete message $messageId in chat $chatId", error)
    }

    override suspend fun sendChatAction(
        chatId: Long,
        action: TelegramAction
    ): Result<Boolean> = runCatching {
        val chatAction = SendChatAction(chatId.toString(), action.value)
        telegramClient.executeAsync(chatAction).await()
    }.onFailure { error ->
        logger.error("Failed to send chat action '$action' to chat $chatId", error)
    }

    override suspend fun answerInlineQuery(
        inlineQueryId: String,
        results: List<InlineQueryResult>,
        cacheTime: Int?,
        isPersonal: Boolean?,
        nextOffset: String?
    ): Result<Boolean> = runCatching {
        val telegramResults = results.map { result ->
            when (result) {
                is twitterx.telegram.api.models.inline.InlineQueryResultArticle -> {
                    val inputContent = result.inputMessageContent as twitterx.telegram.api.models.inline.InputTextMessageContent
                    InlineQueryResultArticle.builder()
                        .id(result.id)
                        .title(result.title)
                        .inputMessageContent(
                            InputTextMessageContent.builder()
                                .messageText(inputContent.messageText)
                                .parseMode(inputContent.parseMode)
                                .disableWebPagePreview(inputContent.disableWebPagePreview)
                                .build()
                        )
                        .description(result.description)
                        .thumbnailUrl(result.thumbUrl)
                        .thumbnailWidth(result.thumbWidth)
                        .thumbnailHeight(result.thumbHeight)
                        .build()
                }
                is twitterx.telegram.api.models.inline.InlineQueryResultPhoto -> {
                    val photoBuilder = InlineQueryResultPhoto.builder()
                        .id(result.id)
                        .photoUrl(result.photoUrl)
                        .thumbnailUrl(result.thumbUrl)
                        .photoWidth(result.photoWidth)
                        .photoHeight(result.photoHeight)
                        .title(result.title)
                        .description(result.description)
                        .caption(result.caption)
                        .parseMode(result.parseMode)
                    
                    if (result.inputMessageContent != null) {
                        val inputContent = result.inputMessageContent as twitterx.telegram.api.models.inline.InputTextMessageContent
                        photoBuilder.inputMessageContent(
                            InputTextMessageContent.builder()
                                .messageText(inputContent.messageText)
                                .parseMode(inputContent.parseMode)
                                .disableWebPagePreview(inputContent.disableWebPagePreview)
                                .build()
                        )
                    }
                    
                    photoBuilder.build()
                }
                else -> throw IllegalArgumentException("Unsupported inline query result type")
            }
        }

        val answerInlineQuery = AnswerInlineQuery(inlineQueryId, telegramResults).apply {
            this.cacheTime = cacheTime
            this.isPersonal = isPersonal
            this.nextOffset = nextOffset
        }

        telegramClient.executeAsync(answerInlineQuery).await()
    }.onFailure { error ->
        logger.error("Failed to answer inline query $inlineQueryId", error)
    }

    /**
     * Converts our InlineKeyboardMarkup to the Telegram library's format.
     */
    private fun InlineKeyboardMarkup.toTelegramMarkup(): org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup {
        val rows = this.inlineKeyboard.map { row ->
            InlineKeyboardRow(
                row.map { button ->
                    InlineKeyboardButton(button.text).apply {
                        callbackData = button.callbackData
                        url = button.url
                    }
                }
            )
        }
        return org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup(rows)
    }
}
