package twitter.app.integration

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import twitter.app.features.twitter.ElonMuskCommandExecutor
import twitter.app.features.twitter.ElonMuskMonitoringService
import twitter.app.repo.SentTweetRepository
import twitter.app.repo.TelegramChat
import twitter.app.repo.TelegramChatRepository
import twitter.app.repo.TweetTrackingRepository
import twitterx.localization.api.LocalizationService
import twitterx.telegram.api.TelegramClient
import twitterx.telegram.api.models.TelegramCommand
import twitterx.telegram.api.models.TelegramConfig
import twitterx.telegram.api.models.TelegramContext
import twitterx.telegram.api.models.response.TelegramMessage
import twitterx.telegram.api.updates.TelegramMessageUpdate
import twitterx.translation.api.Language
import twitterx.translation.api.TranslationService
import twitterx.twitter.api.Tweet
import twitterx.twitter.api.TweetsThread
import twitterx.twitter.api.TwitterService
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import twitterx.telegram.api.models.response.TelegramChat as TelegramChatResponse

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ElonMuskSubscriptionIntegrationTest {

    @Autowired
    private lateinit var telegramChatRepository: TelegramChatRepository

    @Autowired
    private lateinit var sentTweetRepository: SentTweetRepository

    @Autowired
    private lateinit var tweetTrackingRepository: TweetTrackingRepository

    @Autowired
    private lateinit var elonMuskCommandExecutor: ElonMuskCommandExecutor

    @Autowired
    private lateinit var monitoringService: ElonMuskMonitoringService

    @Autowired
    private lateinit var twitterService: TwitterService

    @Autowired
    private lateinit var telegramClient: TelegramClient

    @TestConfiguration
    class TestConfig {
        @Bean
        @Primary
        fun mockTwitterService(): TwitterService = mockk()

        @Bean
        @Primary
        fun mockTelegramClient(): TelegramClient = mockk()

        @Bean
        @Primary
        fun mockTranslationService(): TranslationService = mockk()

        @Bean
        @Primary
        fun mockLocalizationService(): LocalizationService = mockk()
    }

    @BeforeEach
    fun setUp() {
        telegramChatRepository.deleteAll()
        sentTweetRepository.deleteAll()
        tweetTrackingRepository.deleteAll()
    }

    @Test
    fun `should complete full subscription flow`() = runTest {
        // Given
        val chatId = 123L
        val tweetId = "1234567890"
        val update = TelegramMessageUpdate(
            text = "/elonmusk",
            messageId = 1L,
            chatId = chatId,
            command = TelegramCommand.ELONMUSK,
            name = "test-user",
            updateId = 1L
        )
        val config = TelegramConfig("test-token", "test-bot", "123")
        val context = TelegramContext(update, config)
        val tweet = Tweet(
            id = tweetId,
            username = "elonmusk",
            fullName = "Elon Musk",
            content = "Hello world!",
            createdAt = LocalDateTime.now(),
            mediaUrls = emptyList(),
            videoUrls = emptyList(),
            replyToTweetId = null,
            retweetOfTweetId = null,
            quoteTweetId = null,
            profileImageUrl = null,
            language = "en",
            hashtags = emptyList(),
            mentions = emptyList(),
            urls = emptyList()
        )
        val telegramMessage = TelegramMessage(
            messageId = 456L,
            chat = TelegramChatResponse(chatId, "private")
        )

        // Mock external services
        coEvery { telegramClient.sendMessage(any(), any(), any(), any(), any()) } returns Result.success(telegramMessage)
        coEvery { twitterService.getRecentTweetIds("elonmusk", 10) } returns Result.success(listOf(tweetId))
        coEvery { twitterService.getTweet(tweetId) } returns Result.success(tweet)
        coEvery { twitterService.getTweetThread("elonmusk", tweetId) } returns Result.success(TweetsThread.Single(tweet))

        // When - Subscribe to Elon Musk
        elonMuskCommandExecutor.process(context)

        // Then - Check subscription was created
        val savedChat = telegramChatRepository.findById(chatId).orElse(null)
        assertNotNull(savedChat)
        assertTrue(savedChat.isElonMusk)

        // When - Run monitoring
        monitoringService.checkForNewTweets()

        // Then - Check tweet was sent and tracked
        val sentTweets = sentTweetRepository.findByChatIdAndThreadId(chatId, tweetId)
        assertEquals(1, sentTweets.size)
        assertEquals(tweetId, sentTweets[0].tweetId)
        assertEquals(chatId, sentTweets[0].chatId)
        assertEquals(456L, sentTweets[0].messageId)
        assertTrue(sentTweets[0].isMainTweet)

        // Check tracking was updated
        val tweetTracking = tweetTrackingRepository.findByUsername("elonmusk")
        assertNotNull(tweetTracking)
        assertEquals(tweetId, tweetTracking.lastTweetId)
        assertTrue(tweetTracking.isActive)
    }

    @Test
    fun `should handle multiple subscribers`() = runTest {
        // Given
        val chatId1 = 123L
        val chatId2 = 456L
        val tweetId = "1234567890"
        val tweet = Tweet(
            id = tweetId,
            username = "elonmusk",
            fullName = "Elon Musk",
            content = "Hello world!",
            createdAt = LocalDateTime.now(),
            mediaUrls = emptyList(),
            videoUrls = emptyList(),
            replyToTweetId = null,
            retweetOfTweetId = null,
            quoteTweetId = null,
            profileImageUrl = null,
            language = "en",
            hashtags = emptyList(),
            mentions = emptyList(),
            urls = emptyList()
        )
        val telegramMessage = TelegramMessage(
            messageId = 100L,
            chat = TelegramChatResponse(chatId1, "private")
        )

        // Create subscribers
        val subscriber1 = TelegramChat(chatId1, Language.ENGLISH, true)
        val subscriber2 = TelegramChat(chatId2, Language.UKRAINIAN, true)
        telegramChatRepository.saveAll(listOf(subscriber1, subscriber2))

        // Mock external services
        coEvery { telegramClient.sendMessage(any(), any(), any(), any(), any()) } returns Result.success(telegramMessage)
        coEvery { twitterService.getRecentTweetIds("elonmusk", 10) } returns Result.success(listOf(tweetId))
        coEvery { twitterService.getTweet(tweetId) } returns Result.success(tweet)
        coEvery { twitterService.getTweetThread("elonmusk", tweetId) } returns Result.success(TweetsThread.Single(tweet))

        // When
        monitoringService.checkForNewTweets()

        // Then
        val sentTweets1 = sentTweetRepository.findByChatIdAndThreadId(chatId1, tweetId)
        val sentTweets2 = sentTweetRepository.findByChatIdAndThreadId(chatId2, tweetId)

        assertEquals(1, sentTweets1.size)
        assertEquals(1, sentTweets2.size)
        assertEquals(tweetId, sentTweets1[0].tweetId)
        assertEquals(tweetId, sentTweets2[0].tweetId)
        assertEquals(chatId1, sentTweets1[0].chatId)
        assertEquals(chatId2, sentTweets2[0].chatId)
    }

    @Test
    fun `should not send duplicate tweets`() = runTest {
        // Given
        val chatId = 123L
        val tweetId = "1234567890"
        val tweet = Tweet(
            id = tweetId,
            username = "elonmusk",
            fullName = "Elon Musk",
            content = "Hello world!",
            createdAt = LocalDateTime.now(),
            mediaUrls = emptyList(),
            videoUrls = emptyList(),
            replyToTweetId = null,
            retweetOfTweetId = null,
            quoteTweetId = null,
            profileImageUrl = null,
            language = "en",
            hashtags = emptyList(),
            mentions = emptyList(),
            urls = emptyList()
        )
        val telegramMessage = TelegramMessage(
            messageId = 100L,
            chat = TelegramChatResponse(chatId, "private")
        )

        // Create subscriber
        val subscriber = TelegramChat(chatId, Language.ENGLISH, true)
        telegramChatRepository.save(subscriber)

        // Mock external services
        coEvery { telegramClient.sendMessage(any(), any(), any(), any(), any()) } returns Result.success(telegramMessage)
        coEvery { twitterService.getRecentTweetIds("elonmusk", 10) } returns Result.success(listOf(tweetId))
        coEvery { twitterService.getTweet(tweetId) } returns Result.success(tweet)
        coEvery { twitterService.getTweetThread("elonmusk", tweetId) } returns Result.success(TweetsThread.Single(tweet))

        // When - Run monitoring twice
        monitoringService.checkForNewTweets()
        monitoringService.checkForNewTweets()

        // Then - Should only have one sent tweet
        val sentTweets = sentTweetRepository.findByChatIdAndThreadId(chatId, tweetId)
        assertEquals(1, sentTweets.size)
    }

    @Test
    fun `should handle subscription toggle`() = runTest {
        // Given
        val chatId = 123L
        val update = TelegramMessageUpdate(
            text = "/elonmusk",
            messageId = 1L,
            chatId = chatId,
            command = TelegramCommand.ELONMUSK,
            name = "test-user",
            updateId = 1L
        )
        val config = TelegramConfig("test-token", "test-bot", "123")
        val context = TelegramContext(update, config)
        val telegramMessage = TelegramMessage(
            messageId = 100L,
            chat = TelegramChatResponse(chatId, "private")
        )

        // Create initial subscriber
        val subscriber = TelegramChat(chatId, Language.ENGLISH, true)
        telegramChatRepository.save(subscriber)

        coEvery { telegramClient.sendMessage(any(), any(), any(), any(), any()) } returns Result.success(telegramMessage)

        // When - Toggle subscription (should unsubscribe)
        elonMuskCommandExecutor.process(context)

        // Then - Should be unsubscribed
        val updatedChat = telegramChatRepository.findById(chatId).orElse(null)
        assertNotNull(updatedChat)
        assertEquals(false, updatedChat.isElonMusk)

        // When - Toggle again (should subscribe)
        elonMuskCommandExecutor.process(context)

        // Then - Should be subscribed again
        val reSubscribedChat = telegramChatRepository.findById(chatId).orElse(null)
        assertNotNull(reSubscribedChat)
        assertTrue(reSubscribedChat.isElonMusk)
    }

    @Test
    fun `should handle thread with replies`() = runTest {
        // Given
        val chatId = 123L
        val mainTweetId = "1234567890"
        val replyTweetId = "reply123"
        val mainTweet = Tweet(
            id = mainTweetId,
            username = "elonmusk",
            fullName = "Elon Musk",
            content = "Main tweet",
            createdAt = LocalDateTime.now(),
            mediaUrls = emptyList(),
            videoUrls = emptyList(),
            replyToTweetId = null,
            retweetOfTweetId = null,
            quoteTweetId = null,
            profileImageUrl = null,
            language = "en",
            hashtags = emptyList(),
            mentions = emptyList(),
            urls = emptyList()
        )
        val replyTweet = Tweet(
            id = replyTweetId,
            username = "elonmusk",
            fullName = "Elon Musk",
            content = "Reply tweet",
            createdAt = LocalDateTime.now(),
            mediaUrls = emptyList(),
            videoUrls = emptyList(),
            replyToTweetId = mainTweetId,
            retweetOfTweetId = null,
            quoteTweetId = null,
            profileImageUrl = null,
            language = "en",
            hashtags = emptyList(),
            mentions = emptyList(),
            urls = emptyList()
        )
        val mainMessage = TelegramMessage(messageId = 100L, chat = TelegramChatResponse(chatId, "private"))
        val replyMessage = TelegramMessage(messageId = 101L, chat = TelegramChatResponse(chatId, "private"))

        // Create subscriber
        val subscriber = TelegramChat(chatId, Language.ENGLISH, true)
        telegramChatRepository.save(subscriber)

        // Mock external services
        coEvery {
            telegramClient.sendMessage(chatId, any(), "HTML", disableWebPagePreview = true, replyToMessageId = null)
        } returns Result.success(mainMessage)
        coEvery {
            telegramClient.sendMessage(chatId, any(), "HTML", disableWebPagePreview = true, replyToMessageId = 100L)
        } returns Result.success(replyMessage)
        coEvery { twitterService.getRecentTweetIds("elonmusk", 10) } returns Result.success(listOf(mainTweetId))
        coEvery { twitterService.getTweet(mainTweetId) } returns Result.success(mainTweet)
        coEvery { twitterService.getTweetThread("elonmusk", mainTweetId) } returns Result.success(
            TweetsThread.Reply(mainTweet, listOf(replyTweet), null)
        )

        // When
        monitoringService.checkForNewTweets()

        // Then
        val sentTweets = sentTweetRepository.findByChatIdAndThreadId(chatId, mainTweetId)
        assertEquals(2, sentTweets.size)

        val mainSentTweet = sentTweets.find { it.tweetId == mainTweetId }
        val replySentTweet = sentTweets.find { it.tweetId == replyTweetId }

        assertNotNull(mainSentTweet)
        assertNotNull(replySentTweet)
        assertTrue(mainSentTweet.isMainTweet)
        assertEquals(false, replySentTweet.isMainTweet)
        assertEquals(100L, mainSentTweet.messageId)
        assertEquals(101L, replySentTweet.messageId)
    }
}
