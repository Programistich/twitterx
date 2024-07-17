package com.programistich.twitterx.telegram.models

import com.programistich.twitterx.telegram.getCommand
import org.telegram.telegrambots.meta.api.objects.CallbackQuery as TelegramCallbackQuery
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery as TelegramInlineQuery
import org.telegram.telegrambots.meta.api.objects.message.Message as TelegramMessage

sealed class TelegramUpdate {
    class Message(val message: TelegramMessage) : TelegramUpdate()
    class CallbackQuery(val callbackQuery: TelegramCallbackQuery) : TelegramUpdate()
    class InlineQuery(val inlineQuery: TelegramInlineQuery) : TelegramUpdate()

    fun chatId(): Long? {
        return when (this) {
            is Message -> message.chatId
            is CallbackQuery -> callbackQuery.message.chatId
            is InlineQuery -> inlineQuery.from.id
        }
    }

    fun getCommand(botName: String): TelegramCommand? {
        return when (this) {
            is Message -> message.getCommand(botName)
            else -> null
        }
    }
}
