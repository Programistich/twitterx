package twitter.app.telegram

import org.springframework.stereotype.Component
import twitterx.telegram.api.executors.ExecutorProcessor
import twitterx.telegram.api.updates.TelegramUpdate

@Component
public class TelegramExecutorProcessor : ExecutorProcessor {
    override suspend fun process(update: TelegramUpdate) {
        // TODO filter updates by type and chatId
        println(update)
    }
}
