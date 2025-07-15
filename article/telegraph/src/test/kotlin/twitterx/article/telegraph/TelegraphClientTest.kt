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
import twitterx.article.api.ArticleApiException
import twitterx.article.api.ArticleCreationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TelegraphClientTest {

    @Test
    fun `should create account successfully`() = runTest {
        val mockEngine = MockEngine { request ->
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

        val client = createTestClient(mockEngine)
        val telegraphClient = TelegraphClient(client)

        val request = CreateAccountRequest(
            shortName = "Test",
            authorName = "Test Author"
        )

        val account = telegraphClient.createAccount(request)

        assertEquals("Test", account.shortName)
        assertEquals("Test Author", account.authorName)
        assertEquals("test-token", account.accessToken)
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
        val telegraphClient = TelegraphClient(client)

        val request = CreateAccountRequest(shortName = "")

        assertFailsWith<ArticleApiException> {
            telegraphClient.createAccount(request)
        }
    }

    @Test
    fun `should create page successfully`() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = """
                    {
                        "ok":true,
                        "result":{
                            "path":"Test-Page-12-15",
                            "url":"https://telegra.ph/Test-Page-12-15",
                            "title":"Test Page",
                            "description":"Test Description",
                            "views":0
                        }
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = createTestClient(mockEngine)
        val telegraphClient = TelegraphClient(client)

        val request = CreatePageRequest(
            accessToken = "test-token",
            title = "Test Page",
            content = listOf(TelegraphNode.paragraph(listOf(TelegraphNode.text("Test content"))))
        )

        val page = telegraphClient.createPage(request)

        assertEquals("Test-Page-12-15", page.path)
        assertEquals("https://telegra.ph/Test-Page-12-15", page.url)
        assertEquals("Test Page", page.title)
        assertEquals("Test Description", page.description)
        assertEquals(0, page.views)
    }

    @Test
    fun `should handle page creation failure`() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = """{"ok":false,"error":"INVALID_ACCESS_TOKEN"}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = createTestClient(mockEngine)
        val telegraphClient = TelegraphClient(client)

        val request = CreatePageRequest(
            accessToken = "invalid-token",
            title = "Test Page",
            content = listOf(TelegraphNode.paragraph(listOf(TelegraphNode.text("Test content"))))
        )

        assertFailsWith<ArticleCreationException> {
            telegraphClient.createPage(request)
        }
    }

    @Test
    fun `should handle network errors`() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = "Internal Server Error",
                status = HttpStatusCode.InternalServerError
            )
        }

        val client = createTestClient(mockEngine)
        val telegraphClient = TelegraphClient(client)

        val request = CreateAccountRequest(shortName = "Test")

        assertFailsWith<Exception> {
            telegraphClient.createAccount(request)
        }
    }

    private fun createTestClient(mockEngine: MockEngine): HttpClient {
        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }
}
