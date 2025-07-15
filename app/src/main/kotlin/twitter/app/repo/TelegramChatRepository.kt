package twitter.app.repo

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import twitterx.translation.api.Language

public interface TelegramChatRepository : JpaRepository<TelegramChat, Long> {
    @Query("SELECT c FROM TelegramChat c WHERE c.isElonMusk = true")
    public fun findAllElonMuskSubscribers(): List<TelegramChat>
}

@Entity
@Table(name = "telegram_chat")
public data class TelegramChat(
    @Id
    @Column(nullable = false)
    val id: Long,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var language: Language,

    @Column(nullable = false)
    var isElonMusk: Boolean = false
) {
    public constructor(chatId: Long) : this(chatId, Language.ENGLISH, false)
    public constructor(chatId: Long, name: String, language: Language) : this(chatId, language, false)
    public constructor() : this(0, Language.ENGLISH, false)

    public fun idStr(): String = id.toString()
}
