package com.programistich.twitterx.core.telegram.updates

interface TelegramUpdateWithChatId {
    fun chatId(): Long

    fun chatIdString(): String = chatId().toString()
}
