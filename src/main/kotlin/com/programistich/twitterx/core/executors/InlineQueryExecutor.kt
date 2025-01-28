package com.programistich.twitterx.core.executors

import com.programistich.twitterx.core.telegram.updates.TelegramInlineQuery

abstract class InlineQueryExecutor : Executor<TelegramInlineQuery> {
    override val priority: Executor.Priority
        get() = Executor.Priority.HIGH
}
