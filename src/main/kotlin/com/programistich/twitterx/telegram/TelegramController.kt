package com.programistich.twitterx.telegram

import com.programistich.twitterx.entities.TelegramChat
import com.programistich.twitterx.repos.TelegramChatRepository
import com.programistich.twitterx.telegram.models.TelegramConfig
import com.programistich.twitterx.telegram.models.TelegramContext
import com.programistich.twitterx.telegram.models.TelegramUpdate
import com.programistich.twitterx.telegram.processor.TelegramProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.optionals.getOrNull

@Component
class TelegramController(
    private val telegramConfig: TelegramConfig,
    private val telegramProcessors: MutableList<TelegramProcessor>,
    private val chatRepository: TelegramChatRepository,
) : SpringLongPollingBot, LongPollingUpdateConsumer {
    private val channels = ConcurrentHashMap<Long, Channel<TelegramUpdate>>()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun getBotToken(): String = telegramConfig.botToken

    override fun getUpdatesConsumer() = this

    override fun consume(updates: List<Update>) {
        updates
            .asSequence()
            .forEach { update -> scope.launch { processConsume(update) } }
    }

    private suspend fun processConsume(update: Update) {
        val telegramUpdate = update.toTelegramUpdate() ?: return
        val chatId = telegramUpdate.chatId() ?: return

        val channel = channels.computeIfAbsent(chatId) { createChannel() }
        coroutineScope { launch { channel.send(telegramUpdate) } }
    }

    private fun createChannel(): Channel<TelegramUpdate> {
        val channel = Channel<TelegramUpdate>()
        scope.launch {
            for (incomingUpdate in channel) {
                processUpdate(incomingUpdate)
            }
        }
        return channel
    }

    private suspend fun processUpdate(telegramUpdate: TelegramUpdate) {
        val chat = getOrCreateChat(telegramUpdate)
        val command = telegramUpdate.getCommand(telegramConfig.botUsername)
        val context = TelegramContext(telegramUpdate, telegramConfig, command, chat)

        telegramProcessors
            .filter { it.canProcess(context) }
            .maxByOrNull { it.priority.value }
            ?.process(context)
    }

    private suspend fun getOrCreateChat(
        telegramUpdate: TelegramUpdate
    ): TelegramChat? = withContext(Dispatchers.IO) {
        val chatId = telegramUpdate.chatId() ?: return@withContext null

        return@withContext if (!chatRepository.existsById(chatId)) {
            chatRepository.save(TelegramChat(chatId))
        } else {
            chatRepository.findById(chatId).getOrNull()
        }
    }
}
