package com.programistich.twitterx.telegram

import com.programistich.twitterx.entities.ChatLanguage
import com.programistich.twitterx.features.dictionary.Dictionary
import com.programistich.twitterx.telegram.models.TelegramCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand

@Component
class TelegramStarter(
    private val telegramSender: TelegramSender,
    private val dictionary: Dictionary
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    @EventListener(ApplicationReadyEvent::class)
    fun onStart() {
        scope.launch {
            setCommands()
            setMyDescription()
            setMyShortDescription()
            setMyName()
        }
    }

    private suspend fun setCommands() {
        val commands = TelegramCommand.entries.map { BotCommand(it.value, it.description) }
        telegramSender.setMyCommands(commands)
    }

    private suspend fun setMyDescription() {
        ChatLanguage
            .entries
            .forEach {
                val description = dictionary.getByLang("telegram-desc", it)
                telegramSender.setMyDescription(description, it.iso)
            }
    }

    private suspend fun setMyShortDescription() {
        ChatLanguage
            .entries
            .forEach {
                val description = dictionary.getByLang("telegram-short-desc", it)
                telegramSender.setMyShortDescription(description, it.iso)
            }
    }

    private suspend fun setMyName() {
        ChatLanguage
            .entries
            .forEach {
                val description = dictionary.getByLang("telegram-name", it)
                telegramSender.setMyName(description, it.iso)
            }
    }
}
