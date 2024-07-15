package com.programistich.twitterx.features.start

import com.programistich.twitterx.features.dictionary.Dictionary
import com.programistich.twitterx.telegram.TelegramSender
import com.programistich.twitterx.telegram.models.TelegramCommand
import com.programistich.twitterx.telegram.models.TelegramContext
import com.programistich.twitterx.telegram.processor.CommandProcessor
import org.springframework.stereotype.Component

@Component
class StartCommandProcessor(
    private val dictionary: Dictionary,
    private val telegramSender: TelegramSender
) : CommandProcessor() {
    override val command: TelegramCommand
        get() = TelegramCommand.START

    override suspend fun process(context: TelegramContext) {
        val chat = context.chat ?: return
        val text = dictionary.getByLang("start-command-text", chat.language)

        telegramSender.sendText(text, chat.id.toString())
    }
}
