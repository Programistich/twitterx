package twitter.app.repo

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

public interface SentTweetRepository : JpaRepository<SentTweet, Long> {
    @Query("SELECT s FROM SentTweet s WHERE s.tweetId = :tweetId AND s.chatId = :chatId")
    public fun findByTweetIdAndChatId(tweetId: String, chatId: Long): SentTweet?

    @Query("SELECT s FROM SentTweet s WHERE s.chatId = :chatId AND s.parentTweetId = :parentTweetId")
    public fun findByChatIdAndParentTweetId(chatId: Long, parentTweetId: String?): List<SentTweet>

    @Query("SELECT s FROM SentTweet s WHERE s.chatId = :chatId AND s.threadId = :threadId")
    public fun findByChatIdAndThreadId(chatId: Long, threadId: String): List<SentTweet>
}

@Entity
@Table(name = "sent_tweets")
public data class SentTweet(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, length = 100)
    val tweetId: String,

    @Column(nullable = false)
    val chatId: Long,

    @Column(nullable = false)
    val messageId: Long,

    @Column(nullable = true, length = 100)
    val parentTweetId: String?,

    @Column(nullable = true, length = 100)
    val threadId: String?,

    @Column(nullable = false)
    val sentAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    val isMainTweet: Boolean = false
) {
    public constructor() : this(0, "", 0, 0, null, null, LocalDateTime.now(), false)
}
