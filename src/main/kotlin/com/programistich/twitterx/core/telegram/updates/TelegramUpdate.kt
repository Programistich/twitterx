package com.programistich.twitterx.core.telegram.updates

import org.telegram.telegrambots.meta.api.objects.Update

sealed interface TelegramUpdate {
    companion object {
        fun Update.toTelegramUpdate(): TelegramUpdate? {
            return when {
                this.hasMessage() -> TelegramMessageUpdate(this.message)
                this.hasCallbackQuery() -> TelegramCallbackQueryUpdate(this.callbackQuery)
                else -> null
            }
        }
    }
}
