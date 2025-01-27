package com.programistich.twitterx.core.telegram.updates

import com.programistich.twitterx.core.telegram.models.TelegramCommand
import org.telegram.telegrambots.meta.api.objects.EntityType
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.message.Message

class TelegramMessageUpdate(
    private val message: Message
) : TelegramUpdate, TelegramUpdateWithChatId {

    fun getCommand(botName: String): TelegramCommand? {
        return message.getCommand(botName)
    }

    private fun Message.getCommand(botName: String): TelegramCommand? {
        val entities = this.entities ?: return null
        val commandEntity = entities.firstOrNull { it.type == EntityType.BOTCOMMAND } ?: return null
        val commandText = this
            .text
            ?.substring(commandEntity.offset, commandEntity.offset + commandEntity.length)
            ?: return null

        val parts = commandText.split("@")
        return when {
            parts.size == 1 -> TelegramCommand.fromValue(parts[0])
            parts.size == 2 && parts[1] == botName -> TelegramCommand.fromValue(parts[0])
            else -> null
        }
    }

    fun getText(): String? {
        return this.message.text
    }

    fun getFrom(): User? {
        return this.message.from
    }

    fun getUrls(): List<String> {
        return this.message.entities
            ?.filter { it.type == EntityType.URL }
            ?.map { this.message.text.substring(it.offset, it.offset + it.length) }
            ?: emptyList()
    }

    override fun chatId(): Long = message.chatId

    fun messageId(): Int = message.messageId
}
