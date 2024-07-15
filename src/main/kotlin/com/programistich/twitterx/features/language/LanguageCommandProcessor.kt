package com.programistich.twitterx.features.language

import com.programistich.twitterx.entities.ChatLanguage
import com.programistich.twitterx.features.dictionary.Dictionary
import com.programistich.twitterx.telegram.TelegramSender
import com.programistich.twitterx.telegram.models.TelegramCommand
import com.programistich.twitterx.telegram.models.TelegramContext
import com.programistich.twitterx.telegram.processor.CommandProcessor
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow

@Component
class LanguageCommandProcessor(
    private val dictionary: Dictionary,
    private val telegramSender: TelegramSender
) : CommandProcessor() {
    override val command: TelegramCommand
        get() = TelegramCommand.LANG

    override suspend fun process(context: TelegramContext) {
        val chat = context.chat ?: return
        val text = getTextMessage()

        val keyboardMarkup = ChatLanguage
            .entries
            .map { getLanguageButton(it) }
            .map { InlineKeyboardRow(it) }
            .let { InlineKeyboardMarkup(it) }

        telegramSender.sendText(text = text, chatId = chat.id.toString()) {
            replyMarkup = keyboardMarkup
        }
    }

    private fun getTextMessage(): String {
        return ChatLanguage
            .entries
            .joinToString(separator = "\n\n") {
                dictionary.getByLang(
                    table = "language-command-text",
                    language = it
                )
            }
    }

    private fun getLanguageButton(lang: ChatLanguage): InlineKeyboardButton {
        val callback = Json.encodeToString(
            serializer = LanguageCommandCallback.serializer(),
            value = LanguageCommandCallback(lang)
        )
        val textButton = dictionary.getByLang(
            table = "language-command-choose-button",
            language = lang
        )

        return InlineKeyboardButton(textButton).apply { callbackData = callback }
    }
}
