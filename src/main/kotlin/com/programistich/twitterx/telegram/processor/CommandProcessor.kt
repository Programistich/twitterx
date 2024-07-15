package com.programistich.twitterx.telegram.processor

import com.programistich.twitterx.telegram.models.TelegramCommand
import com.programistich.twitterx.telegram.models.TelegramContext
import com.programistich.twitterx.telegram.models.TelegramUpdate
import org.telegram.telegrambots.meta.api.objects.EntityType

abstract class CommandProcessor : TelegramProcessor {
    override suspend fun canProcess(context: TelegramContext): Boolean {
        return command == context.command
    }

    fun getTextWithoutCommand(context: TelegramContext): String? {
        val update = context.update as? TelegramUpdate.Message ?: return null

        val commandEntity = update
            .message
            .entities
            .firstOrNull { it.type == EntityType.BOTCOMMAND }
            ?: return null

        return update.message.text
            ?.removeRange(commandEntity.offset, commandEntity.offset + commandEntity.length)
            ?.trim()
    }

    abstract val command: TelegramCommand

    override val priority: TelegramProcessor.Priority
        get() = TelegramProcessor.Priority.HIGH
}
