package twitterx.telegram.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import twitterx.telegram.api.executors.Executor
import twitterx.telegram.api.models.TelegramConfig
import twitterx.telegram.api.models.TelegramContext
import twitterx.telegram.api.updates.TelegramUpdate
import java.util.concurrent.ConcurrentHashMap

public class TelegramService(
    public val executors: List<Executor<out TelegramUpdate>>,
    public val config: TelegramConfig,
) {
    private val routerScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val channels = ConcurrentHashMap<Long, Channel<TelegramUpdate>>()
    private val logger = LoggerFactory.getLogger(this::class.java)

    public fun consume(updates: List<TelegramUpdate>) {
        logger.debug("Consuming {} updates", updates.size)
        updates
            .asSequence()
            .forEach { update -> routerScope.launch { consumeInternal(update) } }
    }

    private suspend fun consumeInternal(update: TelegramUpdate) {
        val chatId = update.chatId()
        if (chatId == null) {
            logger.debug("Received update without chatId: type={}", update::class.simpleName)
            coroutineScope { launch { process(update) } }
        } else {
            logger.debug("Processing update for chatId: {}, type={}", chatId, update::class.simpleName)
            val channel = channels.computeIfAbsent(chatId) { createChannel(chatId) }
            coroutineScope { launch { channel.send(update) } }
        }
    }

    private fun createChannel(chatId: Long): Channel<TelegramUpdate> {
        logger.debug("Creating new channel for chatId: {}", chatId)
        val channel = Channel<TelegramUpdate>()
        routerScope.launch {
            for (incomingUpdate in channel) {
                try {
                    process(incomingUpdate)
                } catch (exception: Exception) {
                    logger.error("Error processing update for chatId: {}", chatId, exception)
                }
            }
        }
        return channel
    }

    private suspend fun process(update: TelegramUpdate) {
        val context = TelegramContext(update, config)

        val executor = executors
            .sortedByDescending { it.priority.value }
            .map {
                // Hack to avoid unchecked cast
                @Suppress("UNCHECKED_CAST")
                it as Executor<TelegramUpdate>
            }
            .find { executor ->
                runCatching { executor.canProcess(context) }.getOrDefault(false)
            }
            .also { logger.info("Executor $it for update $update ") }
            ?: return

        runCatching {
            executor.process(context)
        }.onFailure {
            logger.error("Error while processing update $update with executor $executor", it)
        }
    }
}
