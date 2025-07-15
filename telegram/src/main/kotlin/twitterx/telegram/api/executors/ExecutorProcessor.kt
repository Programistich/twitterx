package twitterx.telegram.api.executors

import twitterx.telegram.api.updates.TelegramUpdate

public interface ExecutorProcessor {
    public suspend fun process(update: TelegramUpdate)
}
