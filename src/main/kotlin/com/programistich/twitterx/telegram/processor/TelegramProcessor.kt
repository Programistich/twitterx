package com.programistich.twitterx.telegram.processor

import com.programistich.twitterx.telegram.models.TelegramContext

interface TelegramProcessor {
    val priority: Priority
    suspend fun canProcess(context: TelegramContext): Boolean
    suspend fun process(context: TelegramContext)

    enum class Priority(val value: Int) {
        LOW(-1), MEDIUM(0), HIGH(1)
    }
}
