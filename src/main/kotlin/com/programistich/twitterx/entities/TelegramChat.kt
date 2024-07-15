package com.programistich.twitterx.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "telegram_chat")
data class TelegramChat(
    @Id
    @Column(nullable = false)
    val id: Long,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var language: ChatLanguage
) {
    constructor(chatId: Long) : this(chatId, ChatLanguage.EN)
    constructor(chatId: Long, name: String, language: ChatLanguage) : this(chatId, language)
    constructor() : this(0, ChatLanguage.EN)

    fun idStr() = id.toString()
}
