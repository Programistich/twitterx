package twitterx.telegram.api.models

public enum class TelegramCommand(public val value: String) {
    START("/start"),
    ADD("/add"),
    REMOVE("/remove"),
    LIST("/list"),
    LANG("/lang"),
    AI("/ai"),
    ELONMUSK("/elonmusk")
    ;

    public companion object {
        public fun fromValue(value: String): TelegramCommand? {
            return entries.find { it.value == value }
        }
    }
}
