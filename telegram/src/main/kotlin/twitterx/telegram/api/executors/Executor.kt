package twitterx.telegram.api.executors

import twitterx.telegram.api.models.TelegramContext
import twitterx.telegram.api.updates.TelegramUpdate

public interface Executor<UPDATE : TelegramUpdate> {
    public val priority: Priority
    public suspend fun canProcess(context: TelegramContext<UPDATE>): Boolean
    public suspend fun process(context: TelegramContext<UPDATE>)

    public enum class Priority(public val value: Int) {
        LOW(-1), MEDIUM(0), HIGH(1)
    }
}
