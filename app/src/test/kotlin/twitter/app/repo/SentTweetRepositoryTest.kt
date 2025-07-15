package twitter.app.repo

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DataJpaTest
@ActiveProfiles("test")
class SentTweetRepositoryTest {

    @Autowired
    private lateinit var sentTweetRepository: SentTweetRepository

    @BeforeEach
    fun setUp() {
        sentTweetRepository.deleteAll()
    }

    @Test
    fun `should save and find sent tweet by tweet id and chat id`() {
        // Given
        val tweetId = "1234567890"
        val chatId = 123L
        val messageId = 456L
        val sentTweet = SentTweet(
            tweetId = tweetId,
            chatId = chatId,
            messageId = messageId,
            parentTweetId = null,
            threadId = tweetId,
            isMainTweet = true
        )

        // When
        sentTweetRepository.save(sentTweet)
        val found = sentTweetRepository.findByTweetIdAndChatId(tweetId, chatId)

        // Then
        assertNotNull(found)
        assertEquals(tweetId, found.tweetId)
        assertEquals(chatId, found.chatId)
        assertEquals(messageId, found.messageId)
        assertTrue(found.isMainTweet)
    }

    @Test
    fun `should return null when tweet not found`() {
        // Given
        val tweetId = "nonexistent"
        val chatId = 123L

        // When
        val found = sentTweetRepository.findByTweetIdAndChatId(tweetId, chatId)

        // Then
        assertNull(found)
    }

    @Test
    fun `should find tweets by chat id and parent tweet id`() {
        // Given
        val chatId = 123L
        val parentTweetId = "parent123"
        val sentTweet1 = SentTweet(
            tweetId = "reply1",
            chatId = chatId,
            messageId = 101L,
            parentTweetId = parentTweetId,
            threadId = parentTweetId,
            isMainTweet = false
        )
        val sentTweet2 = SentTweet(
            tweetId = "reply2",
            chatId = chatId,
            messageId = 102L,
            parentTweetId = parentTweetId,
            threadId = parentTweetId,
            isMainTweet = false
        )

        // When
        sentTweetRepository.saveAll(listOf(sentTweet1, sentTweet2))
        val found = sentTweetRepository.findByChatIdAndParentTweetId(chatId, parentTweetId)

        // Then
        assertEquals(2, found.size)
        assertTrue(found.all { it.parentTweetId == parentTweetId })
        assertTrue(found.all { it.chatId == chatId })
    }

    @Test
    fun `should find tweets by chat id and thread id`() {
        // Given
        val chatId = 123L
        val threadId = "thread123"
        val sentTweet1 = SentTweet(
            tweetId = "tweet1",
            chatId = chatId,
            messageId = 101L,
            parentTweetId = null,
            threadId = threadId,
            isMainTweet = true
        )
        val sentTweet2 = SentTweet(
            tweetId = "tweet2",
            chatId = chatId,
            messageId = 102L,
            parentTweetId = "tweet1",
            threadId = threadId,
            isMainTweet = false
        )

        // When
        sentTweetRepository.saveAll(listOf(sentTweet1, sentTweet2))
        val found = sentTweetRepository.findByChatIdAndThreadId(chatId, threadId)

        // Then
        assertEquals(2, found.size)
        assertTrue(found.all { it.threadId == threadId })
        assertTrue(found.all { it.chatId == chatId })
    }

    @Test
    fun `should handle thread with replies`() {
        // Given
        val chatId = 123L
        val threadId = "thread123"
        val mainTweet = SentTweet(
            tweetId = threadId,
            chatId = chatId,
            messageId = 100L,
            parentTweetId = null,
            threadId = threadId,
            isMainTweet = true
        )
        val reply1 = SentTweet(
            tweetId = "reply1",
            chatId = chatId,
            messageId = 101L,
            parentTweetId = threadId,
            threadId = threadId,
            isMainTweet = false
        )
        val reply2 = SentTweet(
            tweetId = "reply2",
            chatId = chatId,
            messageId = 102L,
            parentTweetId = "reply1",
            threadId = threadId,
            isMainTweet = false
        )

        // When
        sentTweetRepository.saveAll(listOf(mainTweet, reply1, reply2))
        val threadTweets = sentTweetRepository.findByChatIdAndThreadId(chatId, threadId)
        val replies = sentTweetRepository.findByChatIdAndParentTweetId(chatId, threadId)

        // Then
        assertEquals(3, threadTweets.size)
        assertEquals(1, replies.size)
        assertEquals(1, threadTweets.count { it.isMainTweet })
        assertEquals(2, threadTweets.count { !it.isMainTweet })
    }

    @Test
    fun `should prevent duplicate tweets for same chat`() {
        // Given
        val tweetId = "1234567890"
        val chatId = 123L
        val sentTweet1 = SentTweet(
            tweetId = tweetId,
            chatId = chatId,
            messageId = 101L,
            parentTweetId = null,
            threadId = tweetId,
            isMainTweet = true
        )

        // When
        sentTweetRepository.save(sentTweet1)
        val found = sentTweetRepository.findByTweetIdAndChatId(tweetId, chatId)

        // Then
        assertNotNull(found)
        assertEquals(tweetId, found.tweetId)
        assertEquals(chatId, found.chatId)

        // Should be able to find the same tweet for different chat
        val otherChatId = 456L
        val foundOtherChat = sentTweetRepository.findByTweetIdAndChatId(tweetId, otherChatId)
        assertNull(foundOtherChat)
    }
}
