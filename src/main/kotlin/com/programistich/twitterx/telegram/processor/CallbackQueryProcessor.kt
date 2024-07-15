package com.programistich.twitterx.telegram.processor

import com.programistich.twitterx.telegram.models.TelegramContext
import com.programistich.twitterx.telegram.models.TelegramUpdate
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json

abstract class CallbackQueryProcessor<T> : TelegramProcessor {
    abstract fun deserializer(): DeserializationStrategy<T>

    override suspend fun canProcess(context: TelegramContext): Boolean {
        val update = context.update as? TelegramUpdate.CallbackQuery ?: return false
        return runCatching { getCallbackData(update) }.isSuccess
    }

    fun getCallbackData(update: TelegramUpdate.CallbackQuery): T {
        val callbackData = update.callbackQuery.data
        return Json.decodeFromString(deserializer(), callbackData)
    }

    override val priority: TelegramProcessor.Priority
        get() = TelegramProcessor.Priority.HIGH
}
