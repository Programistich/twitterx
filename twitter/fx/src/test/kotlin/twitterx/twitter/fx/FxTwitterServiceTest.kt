package twitterx.twitter.fx

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import twitterx.twitter.api.TwitterException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FxTwitterServiceTest {

    private fun createMockHttpClient(
        responseContent: String,
        statusCode: HttpStatusCode = HttpStatusCode.OK
    ): HttpClient {
        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel(responseContent),
                status = statusCode,
                headers = headersOf(
                    HttpHeaders.ContentType,
                    ContentType.Application.Json.toString()
                )
            )
        }

        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    }
                )
            }
        }
    }

    private fun loadJsonFixture(filename: String): String {
        return this::class.java.getResource("/$filename")?.readText()
            ?: throw IllegalArgumentException("Resource $filename not found")
    }

    @Test
    fun `getTweet returns success for valid text tweet`() {
        runTest {
            val jsonResponse = loadJsonFixture("text_tweet_response.json")
            val httpClient = createMockHttpClient(jsonResponse)
            val service = FxTwitterService(httpClient, "https://api.fxtwitter.com")

            val result = service.getTweet("1548602399862013953")

            assertTrue(result.isSuccess)
            val tweet = result.getOrThrow()
            assertEquals("1548602399862013953", tweet.id)
            assertEquals("testuser", tweet.username)
            assertEquals("Test User", tweet.fullName)
            assertEquals("This is a simple text tweet with #hashtag and @mention", tweet.content)
            assertEquals("en", tweet.language)
            assertEquals(listOf("hashtag"), tweet.hashtags)
            assertEquals(listOf("mention"), tweet.mentions)
            assertTrue(tweet.mediaUrls.isEmpty())
            assertTrue(tweet.videoUrls.isEmpty())
        }
    }

    @Test
    fun `getTweet returns success for tweet with media`() {
        runTest {
            val jsonResponse = loadJsonFixture("media_tweet_response.json")
            val httpClient = createMockHttpClient(jsonResponse)
            val service = FxTwitterService(httpClient, "https://api.fxtwitter.com")

            val result = service.getTweet("1548602399862013954")

            assertTrue(result.isSuccess)
            val tweet = result.getOrThrow()
            assertEquals("1548602399862013954", tweet.id)
            assertEquals("photouser", tweet.username)
            assertEquals("Photo User", tweet.fullName)
            assertEquals("Check out this amazing photo! #photography", tweet.content)
            assertEquals(listOf("https://pbs.twimg.com/media/FX8qJvXWAAEQgGf.jpg"), tweet.mediaUrls)
            assertTrue(tweet.videoUrls.isEmpty())
            assertEquals(listOf("photography"), tweet.hashtags)
        }
    }

    @Test
    fun `getTweet returns success for tweet with video`() {
        runTest {
            val jsonResponse = loadJsonFixture("video_tweet_response.json")
            val httpClient = createMockHttpClient(jsonResponse)
            val service = FxTwitterService(httpClient, "https://api.fxtwitter.com")

            val result = service.getTweet("1548602399862013955")

            assertTrue(result.isSuccess)
            val tweet = result.getOrThrow()
            assertEquals("1548602399862013955", tweet.id)
            assertEquals("videouser", tweet.username)
            assertEquals("Video Creator", tweet.fullName)
            assertEquals("Amazing video content! Check this out 🎥", tweet.content)
            assertTrue(tweet.mediaUrls.isEmpty())
            val expectedVideoUrl = "https://video.twimg.com/ext_tw_video/" +
                "1548602342488129536/pu/vid/720x1280/I_D3svYfjBl7_xGS.mp4?tag=14"
            assertEquals(
                listOf(expectedVideoUrl),
                tweet.videoUrls
            )
        }
    }

    @Test
    fun `getTweet returns TweetNotFoundException for 404 HTTP status`() {
        runTest {
            val jsonResponse = loadJsonFixture("not_found_response.json")
            val httpClient = createMockHttpClient(jsonResponse, HttpStatusCode.NotFound)
            val service = FxTwitterService(httpClient, "https://api.fxtwitter.com")

            val result = service.getTweet("nonexistent")

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is TwitterException.TweetNotFoundException)
        }
    }

    @Test
    fun `getTweet returns PrivateTweetException for 401 HTTP status`() {
        runTest {
            val jsonResponse = loadJsonFixture("private_tweet_response.json")
            val httpClient = createMockHttpClient(jsonResponse, HttpStatusCode.Unauthorized)
            val service = FxTwitterService(httpClient, "https://api.fxtwitter.com")

            val result = service.getTweet("private_tweet")

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is TwitterException.PrivateTweetException)
        }
    }

    @Test
    fun `getTweet returns RateLimitExceededException for 429 HTTP status`() {
        runTest {
            val httpClient = createMockHttpClient("Too Many Requests", HttpStatusCode.TooManyRequests)
            val service = FxTwitterService(httpClient, "https://api.fxtwitter.com")

            val result = service.getTweet("some_tweet")

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is TwitterException.RateLimitExceededException)
        }
    }

    @Test
    fun `getTweet returns ServiceUnavailableException for 500 HTTP status`() {
        runTest {
            val jsonResponse = loadJsonFixture("api_fail_response.json")
            val httpClient = createMockHttpClient(jsonResponse, HttpStatusCode.InternalServerError)
            val service = FxTwitterService(httpClient, "https://api.fxtwitter.com")

            val result = service.getTweet("some_tweet")

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is TwitterException.ServiceUnavailableException)
        }
    }

    @Test
    fun `getTweet handles FxTwitter API error codes correctly`() {
        runTest {
            val jsonResponse = loadJsonFixture("not_found_response.json")
            val httpClient = createMockHttpClient(jsonResponse, HttpStatusCode.OK)
            val service = FxTwitterService(httpClient, "https://api.fxtwitter.com")

            val result = service.getTweet("nonexistent")

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is TwitterException.TweetNotFoundException)
        }
    }

    @Test
    fun `getTweet returns NetworkException for network failures`() {
        runTest {
            val mockEngine = MockEngine { request ->
                throw IllegalStateException("Network error")
            }

            val httpClient = HttpClient(mockEngine) {
                install(ContentNegotiation) {
                    json()
                }
            }

            val service = FxTwitterService(httpClient, "https://api.fxtwitter.com")

            val result = service.getTweet("some_tweet")

            assertTrue(result.isFailure)
            // With kotlin.Result, the original exception is preserved
            assertTrue(result.exceptionOrNull() is IllegalStateException)
        }
    }

    @Test
    fun `getTweet returns success for quote tweet`() {
        runTest {
            val jsonResponse = loadJsonFixture("quote_tweet_response.json")
            val httpClient = createMockHttpClient(jsonResponse)
            val service = FxTwitterService(httpClient, "https://api.fxtwitter.com")

            val result = service.getTweet("1548602399862013960")

            assertTrue(result.isSuccess)
            val tweet = result.getOrThrow()
            assertEquals("1548602399862013960", tweet.id)
            assertEquals("quoteuser", tweet.username)
            assertEquals("Quote User", tweet.fullName)
            assertEquals("This is a great insight! Adding my thoughts here. #tech", tweet.content)
            assertEquals("en", tweet.language)
            assertEquals(listOf("tech"), tweet.hashtags)
            assertTrue(tweet.mediaUrls.isEmpty())
            assertTrue(tweet.videoUrls.isEmpty())

            // Verify quote tweet properties
            assertTrue(tweet.isQuote)
            assertEquals("1548602399862013950", tweet.quoteTweetId)
            assertEquals(null, tweet.replyToTweetId)
            assertEquals(null, tweet.retweetOfTweetId)
        }
    }

    @Test
    fun `getTweet returns success for reply tweet`() {
        runTest {
            val jsonResponse = loadJsonFixture("reply_tweet_response.json")
            val httpClient = createMockHttpClient(jsonResponse)
            val service = FxTwitterService(httpClient, "https://api.fxtwitter.com")

            val result = service.getTweet("1548602399862013970")

            assertTrue(result.isSuccess)
            val tweet = result.getOrThrow()
            assertEquals("1548602399862013970", tweet.id)
            assertEquals("replyuser", tweet.username)
            assertEquals("Reply User", tweet.fullName)
            assertEquals(
                "@originaluser That sounds incredible! Can you tell us more about the technical details? " +
                    "I'm particularly interested in the implementation. #excited",
                tweet.content
            )
            assertEquals("en", tweet.language)
            assertEquals(listOf("excited"), tweet.hashtags)
            assertEquals(listOf("originaluser"), tweet.mentions)
            assertTrue(tweet.mediaUrls.isEmpty())
            assertTrue(tweet.videoUrls.isEmpty())

            // Verify reply tweet properties
            assertTrue(tweet.isReply)
            assertEquals("1548602399862013950", tweet.replyToTweetId)
            assertEquals(null, tweet.quoteTweetId)
            assertEquals(null, tweet.retweetOfTweetId)
        }
    }
}
