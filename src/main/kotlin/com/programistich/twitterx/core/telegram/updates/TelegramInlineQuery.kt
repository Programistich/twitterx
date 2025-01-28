package com.programistich.twitterx.core.telegram.updates

import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery

class TelegramInlineQuery(
    private val inlineQuery: InlineQuery
) : TelegramUpdate, TelegramUpdateWithChatId {
    override fun chatId(): Long = inlineQuery.from.id

    fun getQuery(): String = inlineQuery.query

    fun getId(): String = inlineQuery.id
}
