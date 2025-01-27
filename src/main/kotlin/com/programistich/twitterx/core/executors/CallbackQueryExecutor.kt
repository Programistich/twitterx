package com.programistich.twitterx.core.executors

import com.programistich.twitterx.core.telegram.models.TelegramContext
import com.programistich.twitterx.core.telegram.updates.TelegramCallbackQueryUpdate
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json

abstract class CallbackQueryExecutor<CALLBACK : Any> : Executor<TelegramCallbackQueryUpdate> {
    override val priority: Executor.Priority
        get() = Executor.Priority.HIGH

    abstract fun getDeserializer(): DeserializationStrategy<CALLBACK>

    override suspend fun canProcess(context: TelegramContext<TelegramCallbackQueryUpdate>): Boolean {
        return runCatching {
            val result: CALLBACK = Json.decodeFromString(
                deserializer = getDeserializer(),
                string = context.update.getData()
            )
            println(result)
        }.isSuccess
    }
}
