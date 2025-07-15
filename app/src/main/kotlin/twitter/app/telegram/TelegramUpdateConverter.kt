package twitter.app.telegram

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.EntityType
import org.telegram.telegrambots.meta.api.objects.Update
import twitterx.telegram.api.models.TelegramCommand
import twitterx.telegram.api.models.TelegramConfig
import twitterx.telegram.api.updates.TelegramCallbackQueryUpdate
import twitterx.telegram.api.updates.TelegramInlineQuery
import twitterx.telegram.api.updates.TelegramMessageUpdate
import twitterx.telegram.api.updates.TelegramUpdate

@Component
public class TelegramUpdateConverter(
    private val telegramConfig: TelegramConfig,
) {
    public fun convert(update: Update): TelegramUpdate? {
        return when {
            update.hasMessage() -> update.toMessageTelegramUpdate()
            update.hasInlineQuery() -> update.toTelegramInlineQuery()
            update.hasCallbackQuery() -> update.toCallbackQueryTelegramUpdate()
            else -> null
        }
    }

    private fun Update.toTelegramInlineQuery(): TelegramInlineQuery? {
        val inlineQuery = this.inlineQuery ?: return null
        return TelegramInlineQuery(
            queryId = inlineQuery.id,
            query = inlineQuery.query,
            updateId = this.updateId.toLong()
        )
    }

    private fun Update.toCallbackQueryTelegramUpdate(): TelegramCallbackQueryUpdate? {
        val callbackQuery = this.callbackQuery ?: return null
        return TelegramCallbackQueryUpdate(
            queryId = callbackQuery.id,
            data = callbackQuery.data ?: "",
            chatId = callbackQuery.message.chatId,
            messageId = callbackQuery.message.messageId.toLong(),
            updateId = this.updateId.toLong()
        )
    }

    private fun Update.toMessageTelegramUpdate(): TelegramMessageUpdate? {
        val message = this.message ?: return null
        return TelegramMessageUpdate(
            text = message.text ?: "",
            messageId = message.messageId.toLong(),
            chatId = message.chat.id,
            command = this.getCommand(),
            name = message.from.firstName,
            updateId = this.updateId.toLong()
        )
    }

    private fun Update.getCommand(): TelegramCommand? {
        val botName = telegramConfig.botUsername
        val entities = message.entities ?: return null
        val text = message.text ?: return null

        val commandEntity = entities.firstOrNull { it.type == EntityType.BOTCOMMAND } ?: return null
        val commandText = this
            .message
            .text
            ?.substring(commandEntity.offset, commandEntity.offset + commandEntity.length)
            ?: return null

        val parts = commandText.split("@")
        if (parts.isEmpty()) return null
        if (!text.startsWith(parts[0])) return null

        return when (parts.size) {
            1 -> TelegramCommand.fromValue(parts[0])
            2 -> if (parts[1] == botName) { TelegramCommand.fromValue(parts[0]) } else { null }
            else -> null
        }
    }
}
