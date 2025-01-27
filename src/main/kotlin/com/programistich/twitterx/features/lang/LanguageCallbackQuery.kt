package com.programistich.twitterx.features.lang

import com.programistich.twitterx.core.executors.CallbackQueryExecutor
import com.programistich.twitterx.core.repos.TelegramChatRepository
import com.programistich.twitterx.core.telegram.models.TelegramContext
import com.programistich.twitterx.core.telegram.updates.TelegramCallbackQueryUpdate
import com.programistich.twitterx.features.dict.DictionaryCache
import com.programistich.twitterx.features.dict.DictionaryKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.generics.TelegramClient

@Component
class LanguageCallbackQuery(
    private val dictionary: DictionaryCache,
    private val chatRepository: TelegramChatRepository,
    private val telegramClient: TelegramClient
) : CallbackQueryExecutor<LanguageCommandCallback>() {
    override fun getDeserializer(): DeserializationStrategy<LanguageCommandCallback> {
        return LanguageCommandCallback.serializer()
    }

    override suspend fun process(context: TelegramContext<TelegramCallbackQueryUpdate>): Result<Unit> {
        val chat = context.chat ?: return Result.failure(Exception("Chat not found"))
        val callback = Json.decodeFromString(
            deserializer = getDeserializer(),
            string = context.update.getData()
        )
        val fromWho = context.update.getFrom()

        withContext(Dispatchers.IO) {
            chatRepository.save(chat.copy(language = callback.newLanguage))
        }

        val text = dictionary.getByKey(DictionaryKey.LANG_CHANGED, callback.newLanguage, fromWho)
        val editMessageText = EditMessageText(text)
        editMessageText.chatId = chat.idStr()
        editMessageText.messageId = context.update.getMessageId()
        val editResult = runCatching {
            telegramClient.executeAsync(editMessageText).await()
        }

        val deleteMessage = DeleteMessage(chat.idStr(), callback.commandMessageId)
        runCatching {
            telegramClient.executeAsync(deleteMessage).await()
        }

        return editResult.map { }
    }
}
