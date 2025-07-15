package twitterx.telegram.api.executors

import twitterx.telegram.api.updates.TelegramInlineQuery

public abstract class InlineQueryExecutor : Executor<TelegramInlineQuery> {
    override val priority: Executor.Priority
        get() = Executor.Priority.HIGH
}
