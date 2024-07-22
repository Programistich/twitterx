package com.programistich.twitterx.telegram.processor

import com.programistich.twitterx.telegram.models.TelegramContext
import com.programistich.twitterx.telegram.models.TelegramUpdate

abstract class BotOwnerProcessor : TelegramProcessor {

    override suspend fun canProcess(context: TelegramContext): Boolean {
        val update = context.update as? TelegramUpdate.Message ?: return false
        return update.message.chatId.toString() == context.config.ownerId && isCanExecuteInternal(context)
    }

    abstract fun isCanExecuteInternal(context: TelegramContext): Boolean

    override val priority: TelegramProcessor.Priority
        get() = TelegramProcessor.Priority.HIGH
}
