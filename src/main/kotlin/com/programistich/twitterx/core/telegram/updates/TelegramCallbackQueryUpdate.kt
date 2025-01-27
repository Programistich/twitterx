package com.programistich.twitterx.core.telegram.updates

import org.telegram.telegrambots.meta.api.objects.CallbackQuery

class TelegramCallbackQueryUpdate(
    private val query: CallbackQuery
) : TelegramUpdate, TelegramUpdateWithChatId {
    fun getData(): String = query.data
    fun getFrom(): String = query.from?.firstName ?: query.from?.lastName ?: query.from?.userName ?: "Unknown"
    fun getMessageId(): Int = query.message.messageId

    override fun chatId(): Long = query.message.chatId
}
