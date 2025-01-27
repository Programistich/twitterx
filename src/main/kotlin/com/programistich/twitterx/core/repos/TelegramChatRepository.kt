package com.programistich.twitterx.core.repos

import com.programistich.twitterx.core.telegram.models.Language
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository

interface TelegramChatRepository : JpaRepository<TelegramChat, Long>

@Entity
@Table(name = "telegram_chat")
data class TelegramChat(
    @Id
    @Column(nullable = false)
    val id: Long,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var language: Language
) {
    constructor(chatId: Long) : this(chatId, Language.EN)
    constructor(chatId: Long, name: String, language: Language) : this(chatId, language)
    constructor() : this(0, Language.EN)

    fun idStr() = id.toString()
}
