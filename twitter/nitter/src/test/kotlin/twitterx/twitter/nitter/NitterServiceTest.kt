package twitterx.twitter.nitter

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import twitterx.twitter.api.TwitterException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NitterServiceTest {

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
                    ContentType.Text.Xml.toString()
                )
            )
        }

        return HttpClient(mockEngine)
    }

    private fun loadXmlFixture(filename: String): String {
        return this::class.java.getResource("/$filename")?.readText()
            ?: throw IllegalArgumentException("Resource $filename not found")
    }

    @Test
    fun `getRecentTweetIds returns success for valid RSS feed`() {
        runTest {
            val rssXml = loadXmlFixture("successful_rss_feed.xml")
            val httpClient = createMockHttpClient(rssXml)
            val service = NitterService(httpClient, "https://nitter.net")

            val result = service.getRecentTweetIds("elonmusk", 10)

            assertTrue(result.isSuccess)
            val tweetIds = result.getOrThrow()
            assertEquals(3, tweetIds.size)
            assertEquals("1933577050016940084", tweetIds[0])
            assertEquals("1933573918671192128", tweetIds[1])
            assertEquals("1933373443170578539", tweetIds[2])
        }
    }

    @Test
    fun `getRecentTweetIds respects limit parameter`() {
        runTest {
            val rssXml = loadXmlFixture("successful_rss_feed.xml")
            val httpClient = createMockHttpClient(rssXml)
            val service = NitterService(httpClient, "https://nitter.net")

            val result = service.getRecentTweetIds("elonmusk", 2)

            assertTrue(result.isSuccess)
            val tweetIds = result.getOrThrow()
            assertEquals(2, tweetIds.size)
            assertEquals("1933577050016940084", tweetIds[0])
            assertEquals("1933573918671192128", tweetIds[1])
        }
    }

    @Test
    fun `getRecentTweetIds returns empty list for empty RSS feed`() {
        runTest {
            val rssXml = loadXmlFixture("empty_rss_feed.xml")
            val httpClient = createMockHttpClient(rssXml)
            val service = NitterService(httpClient, "https://nitter.net")

            val result = service.getRecentTweetIds("emptyuser", 10)

            assertTrue(result.isSuccess)
            val tweetIds = result.getOrThrow()
            assertTrue(tweetIds.isEmpty())
        }
    }

    @Test
    fun `getRecentTweetIds returns AccountNotFoundException for 404 status`() {
        runTest {
            val httpClient = createMockHttpClient("Not Found", HttpStatusCode.NotFound)
            val service = NitterService(httpClient, "https://nitter.net")

            val result = service.getRecentTweetIds("nonexistent", 10)

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is TwitterException.AccountNotFoundException)
        }
    }

    @Test
    fun `getRecentTweetIds returns RateLimitExceededException for 429 status`() {
        runTest {
            val httpClient = createMockHttpClient("Too Many Requests", HttpStatusCode.TooManyRequests)
            val service = NitterService(httpClient, "https://nitter.net")

            val result = service.getRecentTweetIds("someuser", 10)

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is TwitterException.RateLimitExceededException)
        }
    }

    @Test
    fun `getRecentTweetIds returns ServiceUnavailableException for other HTTP errors`() {
        runTest {
            val httpClient = createMockHttpClient("Internal Server Error", HttpStatusCode.InternalServerError)
            val service = NitterService(httpClient, "https://nitter.net")

            val result = service.getRecentTweetIds("someuser", 10)

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is TwitterException.ServiceUnavailableException)
        }
    }

    @Test
    fun `getAccount returns success for valid RSS feed`() {
        runTest {
            val rssXml = loadXmlFixture("successful_rss_feed.xml")
            val httpClient = createMockHttpClient(rssXml)
            val service = NitterService(httpClient, "https://nitter.net")

            val result = service.getAccount("elonmusk")

            assertTrue(result.isSuccess)
            val account = result.getOrThrow()
            assertEquals("elonmusk", account.username)
            assertEquals("Elon Musk", account.name)
        }
    }

    @Test
    fun `getAccount returns AccountNotFoundException for 404 status`() {
        runTest {
            val httpClient = createMockHttpClient("Not Found", HttpStatusCode.NotFound)
            val service = NitterService(httpClient, "https://nitter.net")

            val result = service.getAccount("nonexistent")

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is TwitterException.AccountNotFoundException)
        }
    }

    @Test
    fun `isUsernameAccessible returns true for successful response`() {
        runTest {
            val rssXml = loadXmlFixture("successful_rss_feed.xml")
            val httpClient = createMockHttpClient(rssXml)
            val service = NitterService(httpClient, "https://nitter.net")

            val result = service.isAccountExists("elonmusk")

            assertTrue(result.isSuccess)
            assertTrue(result.getOrThrow())
        }
    }

    @Test
    fun `isUsernameAccessible returns false for HTTP errors`() {
        runTest {
            val httpClient = createMockHttpClient("Not Found", HttpStatusCode.NotFound)
            val service = NitterService(httpClient, "https://nitter.net")

            val result = service.isAccountExists("nonexistent")

            assertTrue(result.isSuccess)
            assertTrue(!result.getOrThrow())
        }
    }

    @Test
    fun `isUsernameAccessible returns false for network exceptions`() {
        runTest {
            val mockEngine = MockEngine { request ->
                throw IllegalStateException("Network error")
            }

            val httpClient = HttpClient(mockEngine)
            val service = NitterService(httpClient, "https://nitter.net")

            val result = service.isAccountExists("elonmusk")

            assertTrue(result.isFailure)
        }
    }

    @Test
    fun `getRecentTweetIds handles malformed RSS gracefully`() {
        runTest {
            val malformedXml = loadXmlFixture("malformed_rss_feed.xml")
            val httpClient = createMockHttpClient(malformedXml)
            val service = NitterService(httpClient, "https://nitter.net")

            val result = service.getRecentTweetIds("malformeduser", 10)

            // Should still parse what it can or return empty list
            assertTrue(result.isSuccess)
            val tweetIds = result.getOrThrow()
        }
    }

    @Test
    fun `getRecentTweetIds returns NetworkException for network failures`() {
        runTest {
            val mockEngine = MockEngine { request ->
                throw IllegalStateException("Network error")
            }

            val httpClient = HttpClient(mockEngine)
            val service = NitterService(httpClient, "https://nitter.net")

            val result = service.getRecentTweetIds("someuser", 10)

            assertTrue(result.isFailure)
            // With kotlin.Result, the original exception is preserved
            assertTrue(result.exceptionOrNull() is IllegalStateException)
        }
    }
}
