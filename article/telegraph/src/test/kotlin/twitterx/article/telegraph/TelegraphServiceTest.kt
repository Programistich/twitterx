package twitterx.article.telegraph

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import twitterx.article.api.ArticleInvalidContentException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TelegraphServiceTest {

    @Test
    fun `should create article successfully`() = runTest {
        val mockEngine = MockEngine { request ->
            when {
                request.url.encodedPath.contains("/createAccount") -> {
                    respond(
                        content = """
                            {
                                "ok":true,
                                "result":{
                                    "short_name":"Test",
                                    "author_name":"Test Author",
                                    "access_token":"test-token"
                                }
                            }
                        """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                request.url.encodedPath.contains("/createPage") -> {
                    respond(
                        content = """
                            {
                                "ok":true,
                                "result":{
                                    "path":"Test-Article-12-15",
                                    "url":"https://telegra.ph/Test-Article-12-15",
                                    "title":"Test Article",
                                    "views":0
                                }
                            }
                        """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                else -> {
                    respond("Not Found", HttpStatusCode.NotFound)
                }
            }
        }

        val client = createTestClient(mockEngine)
        val service = TelegraphService(
            httpClient = client,
            accountShortName = "Test",
            authorName = "Test Author"
        )

        val result = service.createArticle("This is a test article content.", "Test Article")

        assertTrue(result.isSuccess)
        assertEquals("https://telegra.ph/Test-Article-12-15", result.getOrNull())
    }

    @Test
    fun `should handle empty content`() = runTest {
        val mockEngine = MockEngine { request ->
            respond("", HttpStatusCode.OK)
        }

        val client = createTestClient(mockEngine)
        val service = TelegraphService(httpClient = client)

        val result = service.createArticle("", "Test Article")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ArticleInvalidContentException)
    }

    @Test
    fun `should handle empty title`() = runTest {
        val mockEngine = MockEngine { request ->
            respond("", HttpStatusCode.OK)
        }

        val client = createTestClient(mockEngine)
        val service = TelegraphService(httpClient = client)

        val result = service.createArticle("Valid content", "")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ArticleInvalidContentException)
    }

    @Test
    fun `should handle account creation failure`() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = """{"ok":false,"error":"INVALID_SHORT_NAME"}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = createTestClient(mockEngine)
        val service = TelegraphService(httpClient = client)

        val result = service.createArticle("Valid content", "Valid title")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("INVALID_SHORT_NAME") == true)
    }

    @Test
    fun `should handle page creation failure`() = runTest {
        val mockEngine = MockEngine { request ->
            when {
                request.url.encodedPath.contains("/createAccount") -> {
                    respond(
                        content = """
                            {
                                "ok":true,
                                "result":{
                                    "short_name":"Test",
                                    "author_name":"Test Author",
                                    "access_token":"test-token"
                                }
                            }
                        """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                request.url.encodedPath.contains("/createPage") -> {
                    respond(
                        content = """{"ok":false,"error":"CONTENT_TOO_BIG"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                else -> {
                    respond("Not Found", HttpStatusCode.NotFound)
                }
            }
        }

        val client = createTestClient(mockEngine)
        val service = TelegraphService(httpClient = client)

        val result = service.createArticle("Valid content", "Valid title")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("CONTENT_TOO_BIG") == true)
    }

    @Test
    fun `should cache account between requests`() = runTest {
        var accountRequests = 0
        var pageRequests = 0

        val mockEngine = MockEngine { request ->
            when {
                request.url.encodedPath.contains("/createAccount") -> {
                    accountRequests++
                    respond(
                        content = """
                            {
                                "ok":true,
                                "result":{
                                    "short_name":"Test",
                                    "author_name":"Test Author",
                                    "access_token":"test-token"
                                }
                            }
                        """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                request.url.encodedPath.contains("/createPage") -> {
                    pageRequests++
                    respond(
                        content = """
                            {
                                "ok":true,
                                "result":{
                                    "path":"Test-Article-$pageRequests",
                                    "url":"https://telegra.ph/Test-Article-$pageRequests",
                                    "title":"Test Article",
                                    "views":0
                                }
                            }
                        """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                else -> {
                    respond("Not Found", HttpStatusCode.NotFound)
                }
            }
        }

        val client = createTestClient(mockEngine)
        val service = TelegraphService(httpClient = client)

        // First request - should create account
        val result1 = service.createArticle("Content 1", "Title 1")
        assertTrue(result1.isSuccess)
        assertEquals(1, accountRequests)
        assertEquals(1, pageRequests)

        // Second request - should reuse account
        val result2 = service.createArticle("Content 2", "Title 2")
        assertTrue(result2.isSuccess)
        assertEquals(1, accountRequests) // Should not increase
        assertEquals(2, pageRequests)
    }

    private fun createTestClient(mockEngine: MockEngine): HttpClient {
        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }
}
