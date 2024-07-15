package com.programistich.twitterx.telegram

import com.programistich.twitterx.telegram.models.TelegramCommand
import com.programistich.twitterx.telegram.models.TelegramUpdate
import org.telegram.telegrambots.meta.api.objects.EntityType
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.message.Message

fun Update.toTelegramUpdate(): TelegramUpdate? {
    return when {
        this.hasMessage() -> TelegramUpdate.Message(this.message)
        this.hasCallbackQuery() -> TelegramUpdate.CallbackQuery(this.callbackQuery)
        else -> null
    }
}

fun Message.getCommand(botName: String): TelegramCommand? {
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
