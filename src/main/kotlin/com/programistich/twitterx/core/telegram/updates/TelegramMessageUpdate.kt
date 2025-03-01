package com.programistich.twitterx.core.telegram.updates

import com.programistich.twitterx.core.telegram.models.TelegramCommand
import org.telegram.telegrambots.meta.api.objects.EntityType
import org.telegram.telegrambots.meta.api.objects.message.Message

class TelegramMessageUpdate(
    val message: Message
) : TelegramUpdate, TelegramUpdateWithChatId {

    fun getCommand(botName: String): TelegramCommand? {
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

        return when {
            parts.size == 1 -> TelegramCommand.fromValue(parts[0])
            parts.size == 2 && parts[1] == botName -> TelegramCommand.fromValue(parts[0])
            else -> null
        }
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

fun Message.getTextWithoutCommand(): String? = this.text?.split(" ")?.drop(1)?.joinToString(" ")?.trim()
