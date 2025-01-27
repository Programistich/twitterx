package com.programistich.twitterx.features.start

import com.programistich.twitterx.core.executors.CommandExecutor
import com.programistich.twitterx.core.telegram.models.TelegramCommand
import com.programistich.twitterx.core.telegram.models.TelegramContext
import com.programistich.twitterx.core.telegram.updates.TelegramMessageUpdate
import com.programistich.twitterx.features.dict.DictionaryCache
import com.programistich.twitterx.features.dict.DictionaryKey
import kotlinx.coroutines.future.await
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.generics.TelegramClient

@Component
class StartCommandExecutor(
    private val telegramClient: TelegramClient,
    private val dictionary: DictionaryCache
) : CommandExecutor() {
    override val command: TelegramCommand
        get() = TelegramCommand.START

    override suspend fun process(context: TelegramContext<TelegramMessageUpdate>): Result<Unit> {
        val chat = context.chat ?: return Result.failure(IllegalStateException("Chat is null"))
        val text = dictionary.getByKey(key = DictionaryKey.START_COMMAND, language = chat.language)
        val sendMessage = SendMessage(chat.id.toString(), text)

        return runCatching {
            telegramClient.executeAsync(sendMessage).await()
        }
    }
}
