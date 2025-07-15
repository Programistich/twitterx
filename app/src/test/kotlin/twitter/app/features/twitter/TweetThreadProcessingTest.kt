package twitter.app.features.twitter

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import twitter.app.repo.SentTweetRepository
import twitter.app.repo.TelegramChat
import twitter.app.repo.TelegramChatRepository
import twitter.app.repo.TweetTrackingRepository
import twitterx.localization.api.MessageKey
import twitterx.telegram.api.models.response.TelegramMessage
import twitterx.translation.api.Language
import twitterx.translation.api.Translation
import twitterx.twitter.api.Tweet
import twitterx.twitter.api.TweetsThread
import twitterx.twitter.api.TwitterAccount
import twitterx.twitter.api.TwitterService
import java.time.LocalDateTime
import twitterx.telegram.api.models.response.TelegramChat as TelegramChatResponse

class TweetThreadProcessingTest {

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
    fun `should process single tweet thread`() = runTest {
        // Given
        val tweetId = "1234567890"
        val chatId = 123L
        val tweet = createTweet(tweetId, "Single tweet content")
        val subscriber = TelegramChat(chatId, Language.ENGLISH, true)
        val singleThread = TweetsThread.Single(tweet)

        setupMocks(tweetId, tweet, subscriber, singleThread)

        // When
        monitoringService.checkForNewTweets()

        // Then
        coVerify(exactly = 1) {
            telegramClient.sendMessage(chatId, any(), "HTML", disableWebPagePreview = true, replyToMessageId = null)
        }
        coVerify(exactly = 1) { sentTweetRepository.save(any()) }
    }

    @Test
    fun `should process quote thread with original tweet first`() = runTest {
        // Given
        val mainTweetId = "1234567890"
        val originalTweetId = "original123"
        val chatId = 123L
        val mainTweet = createTweet(mainTweetId, "Quote tweet content")
        val originalTweet = createTweet(originalTweetId, "Original tweet content")
        val subscriber = TelegramChat(chatId, Language.ENGLISH, true)
        val quoteThread = TweetsThread.QuoteThread(mainTweet, originalTweet)
        val originalMessage = TelegramMessage(messageId = 100L, chat = TelegramChatResponse(chatId, "private"))
        val quoteMessage = TelegramMessage(messageId = 101L, chat = TelegramChatResponse(chatId, "private"))

        setupMocks(mainTweetId, mainTweet, subscriber, quoteThread)
        coEvery {
            telegramClient.sendMessage(chatId, any(), "HTML", disableWebPagePreview = true, replyToMessageId = null)
        } returns Result.success(originalMessage)
        coEvery {
            telegramClient.sendMessage(chatId, any(), "HTML", disableWebPagePreview = true, replyToMessageId = 100L)
        } returns Result.success(quoteMessage)

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
                replyToMessageId = 100L
            )
        }
        coVerify(exactly = 2) { sentTweetRepository.save(any()) }
    }

    @Test
    fun `should process reply thread in correct order`() = runTest {
        // Given
        val mainTweetId = "1234567890"
        val reply1Id = "reply1"
        val reply2Id = "reply2"
        val chatId = 123L
        val mainTweet = createTweet(mainTweetId, "Main tweet content")
        val reply1 = createTweet(reply1Id, "Reply 1 content", mainTweetId)
        val reply2 = createTweet(reply2Id, "Reply 2 content", reply1Id)
        val subscriber = TelegramChat(chatId, Language.ENGLISH, true)
        val replyThread = TweetsThread.Reply(mainTweet, listOf(reply1, reply2), null)

        val mainMessage = TelegramMessage(messageId = 100L, chat = TelegramChatResponse(chatId, "private"))
        val reply1Message = TelegramMessage(messageId = 101L, chat = TelegramChatResponse(chatId, "private"))
        val reply2Message = TelegramMessage(messageId = 102L, chat = TelegramChatResponse(chatId, "private"))

        setupMocks(mainTweetId, mainTweet, subscriber, replyThread)
        coEvery {
            telegramClient.sendMessage(chatId, any(), "HTML", disableWebPagePreview = true, replyToMessageId = null)
        } returns Result.success(mainMessage)
        coEvery {
            telegramClient.sendMessage(chatId, any(), "HTML", disableWebPagePreview = true, replyToMessageId = 100L)
        } returns Result.success(reply1Message)
        coEvery {
            telegramClient.sendMessage(chatId, any(), "HTML", disableWebPagePreview = true, replyToMessageId = 101L)
        } returns Result.success(reply2Message)

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
                replyToMessageId = 100L
            )
        }
        coVerify {
            telegramClient.sendMessage(
                chatId,
                any(),
                "HTML",
                disableWebPagePreview = true,
                replyToMessageId = 101L
            )
        }
        coVerify(exactly = 3) { sentTweetRepository.save(any()) }
    }

    @Test
    fun `should process retweet thread with retweet info`() = runTest {
        // Given
        val tweetId = "1234567890"
        val chatId = 123L
        val tweet = createTweet(tweetId, "Original tweet content")
        val retweeter = TwitterAccount("retweeter", "Retweeter Name")
        val subscriber = TelegramChat(chatId, Language.ENGLISH, true)
        val retweetThread = TweetsThread.RetweetThread(tweet, retweeter)
        val retweetMessage = TelegramMessage(messageId = 100L, chat = TelegramChatResponse(chatId, "private"))

        setupMocks(tweetId, tweet, subscriber, retweetThread)
        coEvery {
            telegramClient.sendMessage(chatId, any(), "HTML", disableWebPagePreview = true)
        } returns Result.success(retweetMessage)

        // When
        monitoringService.checkForNewTweets()

        // Then
        coVerify {
            telegramClient.sendMessage(
                chatId,
                match { it.contains("🔄 Retweeted by") },
                "HTML",
                disableWebPagePreview = true
            )
        }
        coVerify(exactly = 1) { sentTweetRepository.save(any()) }
    }

    @Test
    fun `should handle mixed language translation in threads`() = runTest {
        // Given
        val mainTweetId = "1234567890"
        val replyId = "reply1"
        val chatId = 123L
        val mainTweet = createTweet(mainTweetId, "Hello world!")
        val replyTweet = createTweet(replyId, "Goodbye world!", mainTweetId)
        val subscriber = TelegramChat(chatId, Language.UKRAINIAN, true)
        val replyThread = TweetsThread.Reply(mainTweet, listOf(replyTweet), null)

        val mainMessage = TelegramMessage(messageId = 100L, chat = TelegramChatResponse(chatId, "private"))
        val replyMessage = TelegramMessage(messageId = 101L, chat = TelegramChatResponse(chatId, "private"))

        setupMocks(mainTweetId, mainTweet, subscriber, replyThread)
        coEvery { translationService.translate("Hello world!", Language.UKRAINIAN) } returns Result.success(
            Translation("Привіт, світ!", Language.UKRAINIAN, "en")
        )
        coEvery { translationService.translate("Goodbye world!", Language.UKRAINIAN) } returns Result.success(
            Translation("До побачення, світ!", Language.UKRAINIAN, "en")
        )
        coEvery {
            telegramClient.sendMessage(chatId, any(), "HTML", disableWebPagePreview = true, replyToMessageId = null)
        } returns Result.success(mainMessage)
        coEvery {
            telegramClient.sendMessage(chatId, any(), "HTML", disableWebPagePreview = true, replyToMessageId = 100L)
        } returns Result.success(replyMessage)

        // When
        monitoringService.checkForNewTweets()

        // Then
        coVerify { translationService.translate("Hello world!", Language.UKRAINIAN) }
        coVerify { translationService.translate("Goodbye world!", Language.UKRAINIAN) }
        coVerify(exactly = 2) {
            telegramClient.sendMessage(chatId, any(), "HTML", disableWebPagePreview = true, replyToMessageId = any())
        }
    }

    @Test
    fun `should handle thread with quoted tweet`() = runTest {
        // Given
        val mainTweetId = "1234567890"
        val replyId = "reply1"
        val quotedTweetId = "quoted123"
        val chatId = 123L
        val mainTweet = createTweet(mainTweetId, "Main tweet content")
        val replyTweet = createTweet(replyId, "Reply content", mainTweetId)
        val quotedTweet = createTweet(quotedTweetId, "Quoted tweet content")
        val subscriber = TelegramChat(chatId, Language.ENGLISH, true)
        val replyThread = TweetsThread.Reply(mainTweet, listOf(replyTweet), quotedTweet)

        setupMocks(mainTweetId, mainTweet, subscriber, replyThread)
        val mainMessage = TelegramMessage(messageId = 100L, chat = TelegramChatResponse(chatId, "private"))
        val replyMessage = TelegramMessage(messageId = 101L, chat = TelegramChatResponse(chatId, "private"))
        coEvery {
            telegramClient.sendMessage(chatId, any(), "HTML", disableWebPagePreview = true, replyToMessageId = null)
        } returns Result.success(mainMessage)
        coEvery {
            telegramClient.sendMessage(chatId, any(), "HTML", disableWebPagePreview = true, replyToMessageId = 100L)
        } returns Result.success(replyMessage)

        // When
        monitoringService.checkForNewTweets()

        // Then
        coVerify(exactly = 2) {
            telegramClient.sendMessage(chatId, any(), "HTML", disableWebPagePreview = true, replyToMessageId = any())
        }
        coVerify(exactly = 2) { sentTweetRepository.save(any()) }
    }

    private fun createTweet(id: String, content: String, replyTo: String? = null): Tweet {
        return Tweet(
            id = id,
            username = "elonmusk",
            fullName = "Elon Musk",
            content = content,
            createdAt = LocalDateTime.now(),
            mediaUrls = emptyList(),
            videoUrls = emptyList(),
            replyToTweetId = replyTo,
            retweetOfTweetId = null,
            quoteTweetId = null,
            profileImageUrl = null,
            language = "en",
            hashtags = emptyList(),
            mentions = emptyList(),
            urls = emptyList()
        )
    }

    private fun setupMocks(
        tweetId: String,
        tweet: Tweet,
        subscriber: TelegramChat,
        thread: TweetsThread
    ) {
        coEvery { tweetTrackingRepository.findByUsername("elonmusk") } returns mockk {
            coEvery { username } returns "elonmusk"
            coEvery { lastTweetId } returns ""
            coEvery { isActive } returns true
        }
        coEvery { twitterService.getRecentTweetIds("elonmusk", 10) } returns Result.success(listOf(tweetId))
        coEvery { twitterService.getTweet(tweetId) } returns Result.success(tweet)
        coEvery { telegramChatRepository.findAllElonMuskSubscribers() } returns listOf(subscriber)
        coEvery { twitterService.getTweetThread("elonmusk", tweetId) } returns Result.success(thread)
        coEvery { sentTweetRepository.findByTweetIdAndChatId(tweetId, subscriber.id) } returns null
        coEvery { translationService.translate(any(), any()) } returns Result.success(
            Translation("translated", subscriber.language, "en")
        )
        coEvery { localizationService.getMessage(MessageKey.TWEET_FROM, any(), any()) } returns "Tweet from @elonmusk"
        coEvery { telegramClient.sendMessage(any(), any(), any(), any(), any()) } returns Result.success(
            TelegramMessage(messageId = 100L, chat = TelegramChatResponse(1L, "private"))
        )
        coEvery { sentTweetRepository.save(any()) } returns mockk()
        coEvery { tweetTrackingRepository.save(any()) } returns mockk()
    }
}
