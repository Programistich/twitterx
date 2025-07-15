package twitterx.telegram.api.updates

public sealed interface TelegramUpdate {
    public val updateId: Long
    public fun chatId(): Long?
}
