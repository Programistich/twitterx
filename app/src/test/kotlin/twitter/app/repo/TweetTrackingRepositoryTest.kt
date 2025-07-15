package twitter.app.repo

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DataJpaTest
@ActiveProfiles("test")
class TweetTrackingRepositoryTest {

    @Autowired
    private lateinit var tweetTrackingRepository: TweetTrackingRepository

    @BeforeEach
    fun setUp() {
        tweetTrackingRepository.deleteAll()
    }

    @Test
    fun `should save and find tweet tracking by username`() {
        // Given
        val username = "elonmusk"
        val tweetId = "1234567890"
        val tweetTracking = TweetTracking(
            username = username,
            lastTweetId = tweetId,
            lastChecked = LocalDateTime.now(),
            isActive = true
        )

        // When
        tweetTrackingRepository.save(tweetTracking)
        val found = tweetTrackingRepository.findByUsername(username)

        // Then
        assertNotNull(found)
        assertEquals(username, found.username)
        assertEquals(tweetId, found.lastTweetId)
        assertTrue(found.isActive)
    }

    @Test
    fun `should return null when username not found`() {
        // Given
        val username = "nonexistent"

        // When
        val found = tweetTrackingRepository.findByUsername(username)

        // Then
        assertNull(found)
    }

    @Test
    fun `should update last tweet id and last checked`() {
        // Given
        val username = "elonmusk"
        val initialTweetId = "1234567890"
        val newTweetId = "0987654321"
        val tweetTracking = TweetTracking(
            username = username,
            lastTweetId = initialTweetId,
            lastChecked = LocalDateTime.now().minusHours(1),
            isActive = true
        )
        tweetTrackingRepository.save(tweetTracking)

        // When
        val found = tweetTrackingRepository.findByUsername(username)!!
        found.lastTweetId = newTweetId
        found.lastChecked = LocalDateTime.now()
        tweetTrackingRepository.save(found)

        // Then
        val updated = tweetTrackingRepository.findByUsername(username)!!
        assertEquals(newTweetId, updated.lastTweetId)
        assertTrue(updated.lastChecked.isAfter(tweetTracking.lastChecked))
    }

    @Test
    fun `should support inactive tracking`() {
        // Given
        val username = "elonmusk"
        val tweetTracking = TweetTracking(
            username = username,
            lastTweetId = "1234567890",
            lastChecked = LocalDateTime.now(),
            isActive = false
        )

        // When
        tweetTrackingRepository.save(tweetTracking)
        val found = tweetTrackingRepository.findByUsername(username)

        // Then
        assertNotNull(found)
        assertEquals(false, found.isActive)
    }
}
