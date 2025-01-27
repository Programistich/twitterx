package com.programistich.twitterx.core.executors

import com.programistich.twitterx.core.telegram.models.TelegramContext
import com.programistich.twitterx.core.telegram.updates.TelegramUpdate

interface Executor<UPDATE : TelegramUpdate> {
    val priority: Priority
    suspend fun canProcess(context: TelegramContext<UPDATE>): Boolean
    suspend fun process(context: TelegramContext<UPDATE>): Result<Unit>

    enum class Priority(val value: Int) {
        LOW(-1), MEDIUM(0), HIGH(1)
    }
}
