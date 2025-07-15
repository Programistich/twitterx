package twitterx.telegram.api.executors

import twitterx.telegram.api.models.TelegramCommand
import twitterx.telegram.api.models.TelegramContext
import twitterx.telegram.api.updates.TelegramMessageUpdate

public abstract class CommandExecutor : Executor<TelegramMessageUpdate> {
    override val priority: Executor.Priority
        get() = Executor.Priority.HIGH

    protected abstract val command: TelegramCommand

    override suspend fun canProcess(context: TelegramContext<TelegramMessageUpdate>): Boolean {
        return context.update.command == command
    }
}
