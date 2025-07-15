package twitterx.twitter.e2e

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import twitterx.twitter.api.TweetsThread
import twitterx.twitter.api.TwitterService
import twitterx.twitter.fx.FxTwitterService
import twitterx.twitter.impl.TwitterServiceImpl
import twitterx.twitter.nitter.NitterService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TwitterE2ETest {

    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                }
            )
        }
        install(Logging) {
            level = LogLevel.BODY
        }
    }

    private val fxTwitterService = FxTwitterService(
        httpClient = httpClient,
        baseUrl = "https://api.fxtwitter.com"
    )

    private val nitterService = NitterService(
        httpClient = httpClient,
        nitterBaseUrl = "http://127.0.0.1:8049" // Adjust to your Nitter instance
    )

    private val twitterService: TwitterService = TwitterServiceImpl(
        idProvider = nitterService,
        tweetProvider = fxTwitterService,
        accountProvider = nitterService
    )

    @Test
    fun `should get single tweet with only text`() = runTest {
        val tweetId = "1942258118710476992"
        val expectedUrl = "https://x.com/heydave7/status/1942258118710476992"

        val result = twitterService.getTweet(tweetId)

        assertTrue(result.isSuccess)
        val tweet = result.getOrThrow()
        assertEquals(tweetId, tweet.id)
        assertEquals("heydave7", tweet.username)
        assertEquals(expectedUrl, tweet.tweetUrl)
        assertTrue(tweet.content.isNotBlank())
        assertTrue(tweet.mediaUrls.isEmpty())
        assertTrue(tweet.videoUrls.isEmpty())
        assertEquals(null, tweet.replyToTweetId)
        assertEquals(null, tweet.quoteTweetId)
    }

    @Test
    fun `should get tweet thread for single tweet`() = runTest {
        val tweetId = "1942258118710476992"
        val username = "heydave7"

        val result = twitterService.getTweetThread(username, tweetId)

        assertTrue(result.isSuccess)
        val thread = result.getOrThrow()
        assertIs<TweetsThread.Single>(thread)
        assertEquals(tweetId, thread.tweet.id)
        assertEquals(username, thread.tweet.username)
    }

    @Test
    fun `should get single tweet with only image`() = runTest {
        val tweetId = "1942228890996408429"
        val expectedUrl = "https://x.com/babaikit/status/1942228890996408429"

        val result = twitterService.getTweet(tweetId)

        assertTrue(result.isSuccess)
        val tweet = result.getOrThrow()
        assertEquals(tweetId, tweet.id)
        assertEquals("babaikit", tweet.username)
        assertEquals(expectedUrl, tweet.tweetUrl)
        assertTrue(tweet.mediaUrls.isNotEmpty())
        assertTrue(tweet.videoUrls.isEmpty())
        assertEquals(null, tweet.replyToTweetId)
        assertEquals(null, tweet.quoteTweetId)
    }

    @Test
    fun `should get single tweet with text and image`() = runTest {
        val tweetId = "1942300094210117808"
        val expectedUrl = "https://x.com/grntmedia/status/1942300094210117808"

        val result = twitterService.getTweet(tweetId)

        assertTrue(result.isSuccess)
        val tweet = result.getOrThrow()
        assertEquals(tweetId, tweet.id)
        assertEquals("grntmedia", tweet.username)
        assertEquals(expectedUrl, tweet.tweetUrl)
        assertTrue(tweet.content.isNotBlank())
        assertTrue(tweet.mediaUrls.isNotEmpty())
        assertTrue(tweet.videoUrls.isEmpty())
        assertEquals(null, tweet.replyToTweetId)
        assertEquals(null, tweet.quoteTweetId)
    }

    @Test
    fun `should get single tweet with multiple images`() = runTest {
        val tweetId = "1942251733066891494"
        val expectedUrl = "https://x.com/TheAppleDesign/status/1942251733066891494"

        val result = twitterService.getTweet(tweetId)

        assertTrue(result.isSuccess)
        val tweet = result.getOrThrow()
        assertEquals(tweetId, tweet.id)
        assertEquals("TheAppleDesign", tweet.username)
        assertEquals(expectedUrl, tweet.tweetUrl)
        assertTrue(tweet.mediaUrls.size > 1)
        assertTrue(tweet.videoUrls.isEmpty())
        assertEquals(null, tweet.replyToTweetId)
        assertEquals(null, tweet.quoteTweetId)
    }

    @Test
    fun `should get single tweet with only video`() = runTest {
        val tweetId = "1942262836660465946"
        val expectedUrl = "https://x.com/NoContextCrap/status/1942262836660465946"

        val result = twitterService.getTweet(tweetId)

        assertTrue(result.isSuccess)
        val tweet = result.getOrThrow()
        assertEquals(tweetId, tweet.id)
        assertEquals("NoContextCrap", tweet.username)
        assertEquals(expectedUrl, tweet.tweetUrl)
        assertTrue(tweet.mediaUrls.isEmpty())
        assertTrue(tweet.videoUrls.isNotEmpty())
    }

    @Test
    fun `should get single tweet with text and video`() = runTest {
        val tweetId = "1942304182897185056"
        val expectedUrl = "https://x.com/Fodorpalaezepa/status/1942304182897185056"

        val result = twitterService.getTweet(tweetId)

        assertTrue(result.isSuccess)
        val tweet = result.getOrThrow()
        assertEquals(tweetId, tweet.id)
        assertEquals("Fodorpalaezepa", tweet.username)
        assertEquals(expectedUrl, tweet.tweetUrl)
        assertTrue(tweet.content.isNotBlank())
        assertTrue(tweet.videoUrls.isNotEmpty())
    }

    @Test
    fun `should get single tweet with text and gif`() = runTest {
        val tweetId = "1942267259541487927"
        val expectedUrl = "https://x.com/Revv180/status/1942267259541487927"

        val result = twitterService.getTweet(tweetId)

        assertTrue(result.isSuccess)
        val tweet = result.getOrThrow()
        assertEquals(tweetId, tweet.id)
        assertEquals("Revv180", tweet.username)
        assertEquals(expectedUrl, tweet.tweetUrl)
        assertTrue(tweet.content.isNotBlank())
        assertTrue(tweet.videoUrls.isNotEmpty())
    }

    @Test
    fun `should get reply tweet with parent chain`() = runTest {
        val replyTweetId = "1942120888771752133"
        val parentTweetId = "1942120394594615617"
        val grandParentTweetId = "1942119635341754538"
        val username = "elonmusk"

        val result = twitterService.getTweetThread(username, replyTweetId)

        assertTrue(result.isSuccess)
        val thread = result.getOrThrow()
        assertIs<TweetsThread.Reply>(thread)

        assertEquals(replyTweetId, thread.tweet.id)
        assertEquals(username, thread.tweet.username)
        assertNotNull(thread.tweet.replyToTweetId)

        assertTrue(thread.replies.isNotEmpty())
        val parentTweet = thread.replies.first()
        assertEquals(parentTweetId, parentTweet.id)
        assertEquals("AutismCapital", parentTweet.username)

        if (thread.replies.size > 1) {
            val grandParentTweet = thread.replies[1]
            assertEquals(grandParentTweetId, grandParentTweet.id)
            assertEquals("elonmusk", grandParentTweet.username)
        }
    }

    @Test
    fun `should get quoted tweet with original`() = runTest {
        val quoteTweetId = "1942128989239459865"
        val originalTweetId = "1941880791908200933"
        val username = "elonmusk"

        val result = twitterService.getTweetThread(username, quoteTweetId)

        assertTrue(result.isSuccess)
        val thread = result.getOrThrow()
        assertIs<TweetsThread.QuoteThread>(thread)

        assertEquals(quoteTweetId, thread.tweet.id)
        assertEquals(username, thread.tweet.username)
        assertNotNull(thread.tweet.quoteTweetId)

        assertEquals(originalTweetId, thread.original.id)
        assertEquals("beinlibertarian", thread.original.username)
    }

    @Test
    fun `should get last tweet ids by username`() = runTest {
        val username = "elonmusk"
        val limit = 15

        val result = twitterService.getRecentTweetIds(username, limit)

        assertTrue(result.isSuccess)
        val tweetIds = result.getOrThrow()
        assertTrue(tweetIds.isNotEmpty())
        assertTrue(tweetIds.size <= limit)

        tweetIds.forEach { tweetId ->
            assertTrue(tweetId.matches(Regex("\\d+")))
            println("https://x.com/$username/status/$tweetId")
        }
    }

    @Test
    fun `should handle private tweet gracefully`() = runTest {
        val privateTweetId = "0000000000000000000"

        val result = twitterService.getTweet(privateTweetId)

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(
            exception?.message?.contains("private", ignoreCase = true) == true ||
                exception?.message?.contains("not found", ignoreCase = true) == true
        )
    }

    @Test
    fun `should validate tweet URL extraction`() = runTest {
        val tweetUrl = "https://x.com/elonmusk/status/1942120888771752133"
        val expectedTweetId = "1942120888771752133"

        val result = twitterService.getTweetId(tweetUrl)

        assertTrue(result.isSuccess)
        assertEquals(expectedTweetId, result.getOrThrow())
    }

    @Test
    fun `should validate account existence`() = runTest {
        val existingUsername = "elonmusk"
        val nonExistingUsername = "nonexistentuser12345678901234567890"

        val existingResult = twitterService.isAccountExists(existingUsername)
        val nonExistingResult = twitterService.isAccountExists(nonExistingUsername)

        assertTrue(existingResult.isSuccess)
        assertTrue(existingResult.getOrThrow())

        assertTrue(nonExistingResult.isSuccess)
        assertTrue(!nonExistingResult.getOrThrow())
    }

    @Test
    fun `should get account information`() = runTest {
        val username = "elonmusk"

        val result = twitterService.getAccount(username)

        assertTrue(result.isSuccess)
        val account = result.getOrThrow()
        assertEquals(username, account.username)
        assertEquals("Elon Musk", account.name)
        assertEquals("https://x.com/$username", account.url)
    }

    @Test
    fun `should get retweet thread with original author`() = runTest {
        val retweetedTweetId = "1942273130980073624"
        val originalTweetId = "1942273130980073624"
        val retweeterUsername = "elonmusk"
        val originalUsername = "techdevnotes"

        val result = twitterService.getTweetThread(retweeterUsername, retweetedTweetId)

        assertTrue(result.isSuccess)
        val thread = result.getOrThrow()
        assertIs<TweetsThread.RetweetThread>(thread)

        // The tweet should be the original tweet
        assertEquals(originalTweetId, thread.tweet.id)
        assertEquals(originalUsername, thread.tweet.username)
        assertEquals("Tech Dev Notes", thread.tweet.fullName)

        // The whoRetweeted should be the retweeter
        assertEquals(retweeterUsername, thread.whoRetweeted.username)
        assertEquals("Elon Musk", thread.whoRetweeted.name)
    }
}
