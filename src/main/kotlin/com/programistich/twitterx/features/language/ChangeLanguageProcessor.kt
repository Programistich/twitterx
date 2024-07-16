package com.programistich.twitterx.features.language

import com.programistich.twitterx.features.dictionary.Dictionary
import com.programistich.twitterx.repos.TelegramChatRepository
import com.programistich.twitterx.telegram.TelegramSender
import com.programistich.twitterx.telegram.models.TelegramContext
import com.programistich.twitterx.telegram.models.TelegramUpdate
import com.programistich.twitterx.telegram.processor.CallbackQueryProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component

@Component
class ChangeLanguageProcessor(
    private val chatRepository: TelegramChatRepository,
    private val dictionary: Dictionary,
    private val telegramSender: TelegramSender
) : CallbackQueryProcessor<LanguageCommandCallback>() {
    companion object {
        private const val MILLI_SECONDS = 1000
        private const val EXPIRED_TIME = 60 * 5 // 5 minutes
    }

    override fun deserializer() = LanguageCommandCallback.serializer()

    override suspend fun process(context: TelegramContext) {
        val update = context.update as? TelegramUpdate.CallbackQuery ?: return
        val chat = context.chat ?: return

        val query = getCallbackData(update)

        val editText = if (isCallBackExpired(update)) {
            dictionary.getByLang("language-callback-expired", chat.language)
        } else {
            chat.language = query.language
            withContext(Dispatchers.IO) { chatRepository.save(chat) }
            dictionary.getByLang("language-callback-success", chat.language)
        }

        telegramSender.editText(
            text = editText,
            chatId = chat.idStr(),
            messageId = update.callbackQuery.message.messageId
        )

        telegramSender.deleteMessage(chat.idStr(), query.messageId)
    }

    private fun isCallBackExpired(update: TelegramUpdate.CallbackQuery): Boolean {
        val updateTime = update.callbackQuery.message?.date ?: return false
        val nowTime = System.currentTimeMillis() / MILLI_SECONDS

        return nowTime - updateTime > EXPIRED_TIME
    }
}
