package com.programistich.twitterx.telegram.models

enum class TelegramCommand(val value: String, val description: String) {
    START("/start", "Start the bot"),
    LANG("/lang", "Change the language"),
    ;

    companion object {
        fun fromValue(value: String): TelegramCommand? = entries.find { it.value == value }
    }
}
