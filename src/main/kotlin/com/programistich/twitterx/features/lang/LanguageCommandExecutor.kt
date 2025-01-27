package com.programistich.twitterx.features.lang

import com.programistich.twitterx.core.executors.CommandExecutor
import com.programistich.twitterx.core.telegram.models.Language
import com.programistich.twitterx.core.telegram.models.TelegramCommand
import com.programistich.twitterx.core.telegram.models.TelegramContext
import com.programistich.twitterx.core.telegram.updates.TelegramMessageUpdate
import com.programistich.twitterx.features.dict.DictionaryCache
import com.programistich.twitterx.features.dict.DictionaryKey
import kotlinx.coroutines.future.await
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow
import org.telegram.telegrambots.meta.generics.TelegramClient

@Component
class LanguageCommandExecutor(
    private val telegramClient: TelegramClient,
    private val dictionary: DictionaryCache
) : CommandExecutor() {
    override val command: TelegramCommand
        get() = TelegramCommand.LANG

    override suspend fun process(context: TelegramContext<TelegramMessageUpdate>): Result<Unit> {
        val chat = context.chat ?: return Result.failure(IllegalStateException("Chat is null"))
        val messageId = context.update.messageId()

        val text = Language.entries.joinToString(separator = "\n\n") { lang ->
            dictionary.getByKey(key = DictionaryKey.LANG_COMMAND, language = lang)
        }

        val sendMessage = SendMessage(chat.id.toString(), text)
        sendMessage.replyMarkup = getInlineKeyboardMarkup(messageId = messageId)

        return runCatching {
            telegramClient.executeAsync(sendMessage).await()
        }
    }

    private fun getInlineKeyboardMarkup(messageId: Int): InlineKeyboardMarkup {
        return Language
            .entries
            .map { language ->
                val callback = LanguageCommandCallback(newLanguage = language, commandMessageId = messageId)
                getLanguageButtonWithData(callback = callback)
            }
            .map { button ->
                InlineKeyboardRow(button)
            }
            .let { row ->
                InlineKeyboardMarkup(row)
            }
    }

    private fun getLanguageButtonWithData(callback: LanguageCommandCallback): InlineKeyboardButton {
        val textButton = dictionary.getByKey(
            key = DictionaryKey.LANG_COMMAND_BUTTON,
            language = callback.newLanguage
        )
        return InlineKeyboardButton(textButton).apply {
            callbackData = callback.encode()
        }
    }
}
