package twitterx.telegram.api.updates

import twitterx.telegram.api.models.TelegramCommand

public class TelegramMessageUpdate(
    public val text: String,
    public val messageId: Long,
    public val chatId: Long,
    public val command: TelegramCommand?,
    public val name: String,
    override val updateId: Long
) : TelegramUpdate {
    override fun chatId(): Long = chatId
}
