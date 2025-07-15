package twitter.app.telegram

import org.springframework.stereotype.Component
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import twitterx.telegram.api.TelegramService
import twitterx.telegram.api.models.TelegramConfig

@Component
public class TelegramController(
    private val telegramConfig: TelegramConfig,
    private val telegramUpdateConverter: TelegramUpdateConverter,
    private val telegramService: TelegramService
) : SpringLongPollingBot, LongPollingUpdateConsumer {
    override fun getBotToken(): String = telegramConfig.botToken

    override fun getUpdatesConsumer(): LongPollingUpdateConsumer = this

    override fun consume(updates: List<Update>) {
        val convertedUpdates = updates.mapNotNull { update ->
            telegramUpdateConverter.convert(update)
        }
        telegramService.consume(convertedUpdates)
    }
}
