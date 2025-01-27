package com.programistich.twitterx.core.executors

import com.programistich.twitterx.core.telegram.models.TelegramCommand
import com.programistich.twitterx.core.telegram.models.TelegramContext
import com.programistich.twitterx.core.telegram.updates.TelegramMessageUpdate

abstract class CommandExecutor : Executor<TelegramMessageUpdate> {
    override val priority: Executor.Priority
        get() = Executor.Priority.HIGH

    protected abstract val command: TelegramCommand

    override suspend fun canProcess(context: TelegramContext<TelegramMessageUpdate>): Boolean {
        return context.update.getCommand(botName = context.config.botUsername) == command
    }
}
