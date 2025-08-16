package twitterx.translation.openai

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import twitterx.translation.api.Language
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OpenAITranslationServiceTest {

    @Test
    fun `test translate english to ukrainian`() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = """{
                "output": [
                  { "id": "rs_x", "type": "reasoning", "summary": [] },
                  { "id": "msg_x", "type": "message", "status": "completed", "role": "assistant",
                    "content": [ { "type": "output_text", "text": "{\n  \"text\": \"Привіт, світ!\",\n  \"sourceLanguage\": \"en\"\n}" } ]
                  }
                ]
            }""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = createTestHttpClient(mockEngine)
        val service = OpenAITranslationService(httpClient, "test-api-key")

        val result = service.translate("Hello, world!", Language.UKRAINIAN)

        assertTrue(result.isSuccess)
        val translation = result.getOrNull()!!
        assertEquals("Привіт, світ!", translation.text)
        assertEquals(Language.UKRAINIAN, translation.to)
        assertEquals("en", translation.from)
    }

    @Test
    fun `test translate ukrainian to english`() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = """{
                "output": [
                  { "id": "rs_x", "type": "reasoning", "summary": [] },
                  { "id": "msg_x", "type": "message", "status": "completed", "role": "assistant",
                    "content": [ { "type": "output_text", "text": "{\n  \"text\": \"Hello, world!\",\n  \"sourceLanguage\": \"uk\"\n}" } ]
                  }
                ]
            }""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = createTestHttpClient(mockEngine)
        val service = OpenAITranslationService(httpClient, "test-api-key")

        val result = service.translate("Привіт, світ!", Language.ENGLISH)

        assertTrue(result.isSuccess)
        val translation = result.getOrNull()!!
        assertEquals("Hello, world!", translation.text)
        assertEquals(Language.ENGLISH, translation.to)
        assertEquals("uk", translation.from)
    }

    @Test
    fun `test same language translation`() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = """{
                "output": [
                  { "id": "rs_x", "type": "reasoning", "summary": [] },
                  { "id": "msg_x", "type": "message", "status": "completed", "role": "assistant",
                    "content": [ { "type": "output_text", "text": "{\n  \"text\": \"Hello, world!\",\n  \"sourceLanguage\": \"en\"\n}" } ]
                  }
                ]
            }""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = createTestHttpClient(mockEngine)
        val service = OpenAITranslationService(httpClient, "test-api-key")

        val result = service.translate("Hello, world!", Language.ENGLISH)

        assertTrue(result.isSuccess)
        val translation = result.getOrNull()!!
        assertTrue(translation.isSameLanguage())
    }

    @Test
    fun `test api error handling`() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = "Internal Server Error",
                status = HttpStatusCode.InternalServerError
            )
        }

        val httpClient = createTestHttpClient(mockEngine)
        val service = OpenAITranslationService(httpClient, "test-api-key")

        val result = service.translate("Hello, world!", Language.UKRAINIAN)

        assertFalse(result.isSuccess)
    }

    @Test
    fun `test empty response handling`() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = """{
                "output": []
            }""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = createTestHttpClient(mockEngine)
        val service = OpenAITranslationService(httpClient, "test-api-key")

        val result = service.translate("Hello, world!", Language.UKRAINIAN)

        assertFalse(result.isSuccess)
    }

    @Test
    fun `test malformed json response handling`() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = """{
                "output": [
                  { "id": "rs_x", "type": "reasoning", "summary": [] },
                  { "id": "msg_x", "type": "message", "status": "completed", "role": "assistant",
                    "content": [ { "type": "output_text", "text": "This is not a valid JSON" } ]
                  }
                ]
            }""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = createTestHttpClient(mockEngine)
        val service = OpenAITranslationService(httpClient, "test-api-key")

        val result = service.translate("Hello, world!", Language.UKRAINIAN)

        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull()?.message?.contains("Failed to parse structured response") == true)
    }

    private fun createTestHttpClient(mockEngine: MockEngine): HttpClient {
        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    }
                )
            }
        }
    }
}
