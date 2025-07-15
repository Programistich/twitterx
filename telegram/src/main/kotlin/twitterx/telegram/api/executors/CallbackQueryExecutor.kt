package twitterx.telegram.api.executors

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import twitterx.telegram.api.models.TelegramContext
import twitterx.telegram.api.updates.TelegramCallbackQueryUpdate

public abstract class CallbackQueryExecutor<CALLBACK : Any> : Executor<TelegramCallbackQueryUpdate> {
    override val priority: Executor.Priority
        get() = Executor.Priority.HIGH

    public abstract fun getDeserializer(): DeserializationStrategy<CALLBACK>

    override suspend fun canProcess(context: TelegramContext<TelegramCallbackQueryUpdate>): Boolean {
        return runCatching {
            Json.decodeFromString(
                deserializer = getDeserializer(),
                string = context.update.data
            )
        }.isSuccess
    }
}
