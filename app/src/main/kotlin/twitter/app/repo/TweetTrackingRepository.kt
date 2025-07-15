package twitter.app.repo

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

public interface TweetTrackingRepository : JpaRepository<TweetTracking, String> {
    @Query("SELECT t FROM TweetTracking t WHERE t.username = :username")
    public fun findByUsername(username: String): TweetTracking?
}

@Entity
@Table(name = "tweet_tracking")
public data class TweetTracking(
    @Id
    @Column(nullable = false, length = 50)
    val username: String,

    @Column(nullable = false, length = 100)
    var lastTweetId: String,

    @Column(nullable = false)
    var lastChecked: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var isActive: Boolean = true
) {
    public constructor() : this("", "", LocalDateTime.now(), false)
}
