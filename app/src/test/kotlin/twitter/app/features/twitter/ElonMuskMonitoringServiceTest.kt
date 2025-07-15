package twitter.app.features.twitter

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import twitter.app.repo.SentTweet
import twitter.app.repo.SentTweetRepository
import twitter.app.repo.TelegramChat
import twitter.app.repo.TelegramChatRepository
import twitter.app.repo.TweetTracking
import twitter.app.repo.TweetTrackingRepository
import twitterx.localization.api.MessageKey
import twitterx.telegram.api.models.response.TelegramMessage
import twitterx.translation.api.Language
import twitterx.translation.api.Translation
import twitterx.twitter.api.Tweet
import twitterx.twitter.api.TweetsThread
import twitterx.twitter.api.TwitterService
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import twitterx.telegram.api.models.response.TelegramChat as TelegramChatResponse

class ElonMuskMonitoringServiceTest {

    private val twitterService = mockk<TwitterService>()
    private val telegramChatRepository = mockk<TelegramChatRepository>()
    private val tweetTrackingRepository = mockk<TweetTrackingRepository>()
    private val sentTweetRepository = mockk<SentTweetRepository>()
    private val tweetSender = mockk<TweetSender>()

    private lateinit var monitoringService: ElonMuskMonitoringService

    @BeforeEach
    fun setUp() {
        monitoringService = ElonMuskMonitoringService(
            twitterService = twitterService,
            telegramChatRepository = telegramChatRepository,
            tweetTrackingRepository = tweetTrackingRepository,
            sentTweetRepository = sentTweetRepository,
            tweetSender = tweetSender
        )
    }

    @Test
    fun `should skip monitoring when tracking is inactive`() = runTest {
        // Given
        val tweetTracking = TweetTracking(
            username = "elonmusk",
            lastTweetId = "123",
            lastChecked = LocalDateTime.now(),
            isActive = false
        )
        coEvery { tweetTrackingRepository.findByUsername("elonmusk") } returns tweetTracking

        // When
        monitoringService.checkForNewTweets()

        // Then
        coVerify(exactly = 0) { twitterService.getRecentTweetIds(any(), any()) }
        coVerify(exactly = 0) { telegramChatRepository.findAllElonMuskSubscribers() }
    }

    @Test
    fun `should process new tweets for subscribers`() = runTest {
        // Given
        val tweetId = "1234567890"
        val chatId = 123L
        val messageId = 456L
        val tweetTracking = TweetTracking(
            username = "elonmusk",
            lastTweetId = "",
            lastChecked = LocalDateTime.now(),
            isActive = true
        )
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
        val subscriber = TelegramChat(chatId, Language.ENGLISH, true)
        val telegramMessage = TelegramMessage(
            messageId = messageId,
            chat = TelegramChatResponse(chatId, "private")
        )
        val savedTweetSlot = slot<SentTweet>()

        coEvery { tweetTrackingRepository.findByUsername("elonmusk") } returns tweetTracking
        coEvery { twitterService.getRecentTweetIds("elonmusk", 10) } returns Result.success(listOf(tweetId))
        coEvery { twitterService.getTweet(tweetId) } returns Result.success(tweet)
        coEvery { telegramChatRepository.findAllElonMuskSubscribers() } returns listOf(subscriber)
        coEvery { twitterService.getTweetThread("elonmusk", tweetId) } returns Result.success(TweetsThread.Single(tweet))
        coEvery { sentTweetRepository.findByTweetIdAndChatId(tweetId, chatId) } returns null
        coEvery { translationService.translate(tweet.content, Language.ENGLISH) } returns Result.success(
            Translation(tweet.content, Language.ENGLISH, "en")
        )
        coEvery {
            localizationService.getMessage(MessageKey.TWEET_FROM, Language.ENGLISH, any())
        } returns "Tweet from @elonmusk"
        coEvery {
            telegramClient.sendMessage(chatId, any(), "HTML", disableWebPagePreview = true, replyToMessageId = null)
        } returns Result.success(telegramMessage)
        coEvery { sentTweetRepository.save(capture(savedTweetSlot)) } returns mockk()
        coEvery { tweetTrackingRepository.save(any()) } returns mockk()

        // When
        monitoringService.checkForNewTweets()

        // Then
        coVerify { twitterService.getRecentTweetIds("elonmusk", 10) }
        coVerify { twitterService.getTweet(tweetId) }
        coVerify { telegramChatRepository.findAllElonMuskSubscribers() }
        coVerify { twitterService.getTweetThread("elonmusk", tweetId) }
        coVerify {
            telegramClient.sendMessage(
                chatId,
                any(),
                "HTML",
                disableWebPagePreview = true,
                replyToMessageId = null
            )
        }
        coVerify { sentTweetRepository.save(any()) }
        coVerify { tweetTrackingRepository.save(any()) }

        // Verify saved tweet
        val savedTweet = savedTweetSlot.captured
        assertEquals(tweetId, savedTweet.tweetId)
        assertEquals(chatId, savedTweet.chatId)
        assertEquals(messageId, savedTweet.messageId)
        assertTrue(savedTweet.isMainTweet)
    }

    @Test
    fun `should skip already sent tweets`() = runTest {
        // Given
        val tweetId = "1234567890"
        val chatId = 123L
        val tweetTracking = TweetTracking(
            username = "elonmusk",
            lastTweetId = "",
            lastChecked = LocalDateTime.now(),
            isActive = true
        )
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
        val subscriber = TelegramChat(chatId, Language.ENGLISH, true)
        val existingSentTweet = SentTweet(
            tweetId = tweetId,
            chatId = chatId,
            messageId = 456L,
            parentTweetId = null,
            threadId = tweetId,
            isMainTweet = true
        )

        coEvery { tweetTrackingRepository.findByUsername("elonmusk") } returns tweetTracking
        coEvery { twitterService.getRecentTweetIds("elonmusk", 10) } returns Result.success(listOf(tweetId))
        coEvery { twitterService.getTweet(tweetId) } returns Result.success(tweet)
        coEvery { telegramChatRepository.findAllElonMuskSubscribers() } returns listOf(subscriber)
        coEvery { twitterService.getTweetThread("elonmusk", tweetId) } returns Result.success(TweetsThread.Single(tweet))
        coEvery { sentTweetRepository.findByTweetIdAndChatId(tweetId, chatId) } returns existingSentTweet
        coEvery { tweetTrackingRepository.save(any()) } returns mockk()

        // When
        monitoringService.checkForNewTweets()

        // Then
        coVerify { sentTweetRepository.findByTweetIdAndChatId(tweetId, chatId) }
        coVerify(exactly = 0) { telegramClient.sendMessage(any(), any(), any(), any(), any()) }
        coVerify(exactly = 0) { sentTweetRepository.save(any()) }
    }

    @Test
    fun `should handle reply threads correctly`() = runTest {
        // Given
        val mainTweetId = "1234567890"
        val replyTweetId = "0987654321"
        val chatId = 123L
        val mainMessageId = 100L
        val replyMessageId = 101L

        val tweetTracking = TweetTracking(
            username = "elonmusk",
            lastTweetId = "",
            lastChecked = LocalDateTime.now(),
            isActive = true
        )
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
        val subscriber = TelegramChat(chatId, Language.ENGLISH, true)
        val mainTelegramMessage = TelegramMessage(
            messageId = mainMessageId,
            chat = TelegramChatResponse(chatId, "private")
        )
        val replyTelegramMessage = TelegramMessage(
            messageId = replyMessageId,
            chat = TelegramChatResponse(chatId, "private")
        )
        val replyThread = TweetsThread.Reply(
            tweet = mainTweet,
            replies = listOf(replyTweet),
            quotedTweet = null
        )

        coEvery { tweetTrackingRepository.findByUsername("elonmusk") } returns tweetTracking
        coEvery { twitterService.getRecentTweetIds("elonmusk", 10) } returns Result.success(listOf(mainTweetId))
        coEvery { twitterService.getTweet(mainTweetId) } returns Result.success(mainTweet)
        coEvery { telegramChatRepository.findAllElonMuskSubscribers() } returns listOf(subscriber)
        coEvery { twitterService.getTweetThread("elonmusk", mainTweetId) } returns Result.success(replyThread)
        coEvery { sentTweetRepository.findByTweetIdAndChatId(mainTweetId, chatId) } returns null
        coEvery { translationService.translate(any(), Language.ENGLISH) } returns Result.success(
            Translation("translated", Language.ENGLISH, "en")
        )
        coEvery {
            localizationService.getMessage(MessageKey.TWEET_FROM, Language.ENGLISH, any())
        } returns "Tweet from @elonmusk"
        coEvery {
            telegramClient.sendMessage(chatId, any(), "HTML", disableWebPagePreview = true, replyToMessageId = null)
        } returns Result.success(mainTelegramMessage)
        coEvery {
            telegramClient.sendMessage(chatId, any(), "HTML", disableWebPagePreview = true, replyToMessageId = mainMessageId)
        } returns Result.success(replyTelegramMessage)
        coEvery { sentTweetRepository.save(any()) } returns mockk()
        coEvery { tweetTrackingRepository.save(any()) } returns mockk()

        // When
        monitoringService.checkForNewTweets()

        // Then
        coVerify {
            telegramClient.sendMessage(
                chatId,
                any(),
                "HTML",
                disableWebPagePreview = true,
                replyToMessageId = null
            )
        }
        coVerify {
            telegramClient.sendMessage(
                chatId,
                any(),
                "HTML",
                disableWebPagePreview = true,
                replyToMessageId = mainMessageId
            )
        }
        coVerify(exactly = 2) { sentTweetRepository.save(any()) }
    }

    @Test
    fun `should translate tweets to user language`() = runTest {
        // Given
        val tweetId = "1234567890"
        val chatId = 123L
        val tweetTracking = TweetTracking(
            username = "elonmusk",
            lastTweetId = "",
            lastChecked = LocalDateTime.now(),
            isActive = true
        )
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
        val subscriber = TelegramChat(chatId, Language.UKRAINIAN, true)
        val translation = Translation("Привіт, світ!", Language.UKRAINIAN, "en")
        val telegramMessage = TelegramMessage(
            messageId = 456L,
            chat = TelegramChatResponse(chatId, "private")
        )
        val messageSlot = slot<String>()

        coEvery { tweetTrackingRepository.findByUsername("elonmusk") } returns tweetTracking
        coEvery { twitterService.getRecentTweetIds("elonmusk", 10) } returns Result.success(listOf(tweetId))
        coEvery { twitterService.getTweet(tweetId) } returns Result.success(tweet)
        coEvery { telegramChatRepository.findAllElonMuskSubscribers() } returns listOf(subscriber)
        coEvery { twitterService.getTweetThread("elonmusk", tweetId) } returns Result.success(TweetsThread.Single(tweet))
        coEvery { sentTweetRepository.findByTweetIdAndChatId(tweetId, chatId) } returns null
        coEvery { translationService.translate(tweet.content, Language.UKRAINIAN) } returns Result.success(translation)
        coEvery {
            localizationService.getMessage(MessageKey.TWEET_FROM, Language.UKRAINIAN, any())
        } returns "Твіт від @elonmusk"
        coEvery {
            telegramClient.sendMessage(chatId, capture(messageSlot), "HTML", disableWebPagePreview = true, replyToMessageId = null)
        } returns Result.success(telegramMessage)
        coEvery { sentTweetRepository.save(any()) } returns mockk()
        coEvery { tweetTrackingRepository.save(any()) } returns mockk()

        // When
        monitoringService.checkForNewTweets()

        // Then
        coVerify { translationService.translate(tweet.content, Language.UKRAINIAN) }
        val sentMessage = messageSlot.captured
        assertTrue(sentMessage.contains("Привіт, світ!"))
        assertTrue(sentMessage.contains("Hello world!"))
    }

    @Test
    fun `should update tweet tracking after processing`() = runTest {
        // Given
        val tweetId = "1234567890"
        val tweetTracking = TweetTracking(
            username = "elonmusk",
            lastTweetId = "",
            lastChecked = LocalDateTime.now().minusHours(1),
            isActive = true
        )
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
        val subscriber = TelegramChat(123L, Language.ENGLISH, true)
        val telegramMessage = TelegramMessage(
            messageId = 456L,
            chat = TelegramChatResponse(123L, "private")
        )
        val trackingSlot = slot<TweetTracking>()

        coEvery { tweetTrackingRepository.findByUsername("elonmusk") } returns tweetTracking
        coEvery { twitterService.getRecentTweetIds("elonmusk", 10) } returns Result.success(listOf(tweetId))
        coEvery { twitterService.getTweet(tweetId) } returns Result.success(tweet)
        coEvery { telegramChatRepository.findAllElonMuskSubscribers() } returns listOf(subscriber)
        coEvery { twitterService.getTweetThread("elonmusk", tweetId) } returns Result.success(TweetsThread.Single(tweet))
        coEvery { sentTweetRepository.findByTweetIdAndChatId(tweetId, 123L) } returns null
        coEvery { translationService.translate(any(), any()) } returns Result.success(
            Translation("translated", Language.ENGLISH, "en")
        )
        coEvery { localizationService.getMessage(any(), any(), any()) } returns "Tweet from @elonmusk"
        coEvery { telegramClient.sendMessage(any(), any(), any(), any(), any()) } returns Result.success(telegramMessage)
        coEvery { sentTweetRepository.save(any()) } returns mockk()
        coEvery { tweetTrackingRepository.save(capture(trackingSlot)) } returns mockk()

        // When
        monitoringService.checkForNewTweets()

        // Then
        val savedTracking = trackingSlot.captured
        assertEquals(tweetId, savedTracking.lastTweetId)
        assertTrue(!savedTracking.lastChecked.isBefore(tweetTracking.lastChecked))
    }
}
