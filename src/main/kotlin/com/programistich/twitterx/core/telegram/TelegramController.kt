package com.programistich.twitterx.core.telegram

import com.programistich.twitterx.core.executors.Executor
import com.programistich.twitterx.core.repos.TelegramChat
import com.programistich.twitterx.core.repos.TelegramChatRepository
import com.programistich.twitterx.core.telegram.models.TelegramConfig
import com.programistich.twitterx.core.telegram.models.TelegramContext
import com.programistich.twitterx.core.telegram.updates.TelegramUpdate
import com.programistich.twitterx.core.telegram.updates.TelegramUpdate.Companion.toTelegramUpdate
import com.programistich.twitterx.core.telegram.updates.TelegramUpdateWithChatId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.optionals.getOrNull

@Component
class TelegramController(
    private val telegramConfig: TelegramConfig,
    private val telegramExecutors: List<Executor<out TelegramUpdate>>,
    private val chatRepository: TelegramChatRepository,
) : SpringLongPollingBot, LongPollingUpdateConsumer {
    private val channels = ConcurrentHashMap<Long, Channel<TelegramUpdate>>()
    private val routerScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun getBotToken(): String = telegramConfig.botToken

    override fun getUpdatesConsumer() = this

    override fun consume(updates: List<Update>) {
        updates
            .asSequence()
            .forEach { update -> routerScope.launch { consumeInternal(update) } }
    }

    private suspend fun consumeInternal(update: Update) {
        println(update)
        val telegramUpdate = update.toTelegramUpdate() ?: return
        val chatId = (telegramUpdate as? TelegramUpdateWithChatId)?.chatId() ?: return

        val channel = channels.computeIfAbsent(chatId) { createChannel() }
        coroutineScope { launch { channel.send(telegramUpdate) } }
    }

    private fun createChannel(): Channel<TelegramUpdate> {
        val channel = Channel<TelegramUpdate>()
        routerScope.launch {
            for (incomingUpdate in channel) {
                processUpdate(incomingUpdate)
            }
        }
        return channel
    }

    private suspend fun processUpdate(telegramUpdate: TelegramUpdate) {
        val chat = getOrCreateChat(telegramUpdate as? TelegramUpdateWithChatId)
        val context = TelegramContext(telegramUpdate, telegramConfig, chat)

        val executor = telegramExecutors
            .sortedByDescending { it.priority.value }
            .map {
                // Hack to avoid unchecked cast
                @Suppress("UNCHECKED_CAST")
                it as Executor<TelegramUpdate>
            }
            .find { executor ->
                runCatching { executor.canProcess(context) }.getOrDefault(false)
            }
            .also { logger.info("Executor $it for update $telegramUpdate ") }
            ?: return

        try {
            executor.process(context).getOrThrow()
        } catch (exception: TelegramApiRequestException) {
            logger.error("Error while processing telegram update $telegramUpdate executor $executor", exception)
        } catch (exception: Exception) {
            logger.error("Error while processing update $telegramUpdate executor $executor", exception)
        }
    }

    private suspend fun getOrCreateChat(
        telegramUpdate: TelegramUpdateWithChatId?
    ): TelegramChat? = withContext(Dispatchers.IO) {
        val chatId = telegramUpdate?.chatId() ?: return@withContext null

        return@withContext if (!chatRepository.existsById(chatId)) {
            chatRepository.save(TelegramChat(chatId))
        } else {
            chatRepository.findById(chatId).getOrNull()
        }
    }
}
