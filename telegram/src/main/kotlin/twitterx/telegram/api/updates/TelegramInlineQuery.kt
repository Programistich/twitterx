package twitterx.telegram.api.updates

public data class TelegramInlineQuery(
    public val queryId: String,
    public val query: String,
    override val updateId: Long
) : TelegramUpdate {
    override fun chatId(): Long? = null // Inline queries don't have chat context
}
