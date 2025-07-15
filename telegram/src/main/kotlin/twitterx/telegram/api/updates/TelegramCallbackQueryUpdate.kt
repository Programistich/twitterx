package twitterx.telegram.api.updates

public data class TelegramCallbackQueryUpdate(
    public val queryId: String,
    public val data: String,
    public val chatId: Long,
    public val messageId: Long,
    override val updateId: Long
) : TelegramUpdate {
    override fun chatId(): Long = chatId
}
