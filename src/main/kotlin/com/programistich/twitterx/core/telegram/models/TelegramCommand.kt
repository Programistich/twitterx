package com.programistich.twitterx.core.telegram.models

enum class TelegramCommand(val value: String) {
    START("/start"),
    LANG("/lang"),
    GPT("/gpt")
    ;

    companion object {
        fun fromValue(value: String): TelegramCommand? {
            return entries.find { it.value == value }
        }
    }
}
