package twitter.translations.qween

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

class QwenTranslationServiceTest {

    @Test
    fun `test translate english to ukrainian`() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = """{
                    "choices": [{
                        "message": {
                            "role": "assistant",
                            "content": "{\"text\": \"Привіт, світ!\", \"sourceLanguage\": \"en\"}"
                        }
                    }]
                }""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    }
                )
            }
        }

        val service = QwenTranslationService(httpClient, "test-api-key")

        val result = service.translate("Hello, world!", Language.UKRAINIAN)

        assertTrue(result.isSuccess)
        val translation = result.getOrNull()!!
        assertEquals("Привіт, світ!", translation.text)
        assertEquals(Language.UKRAINIAN, translation.to)
        assertEquals("en", translation.from)
    }

    @Test
    fun `test translate ukrainian to english`() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = """{
                    "choices": [{
                        "message": {
                            "role": "assistant",
                            "content": "{\"text\": \"Hello, world!\", \"sourceLanguage\": \"uk\"}"
                        }
                    }]
                }""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    }
                )
            }
        }

        val service = QwenTranslationService(httpClient, "test-api-key")

        val result = service.translate("Привіт, світ!", Language.ENGLISH)

        assertTrue(result.isSuccess)
        val translation = result.getOrNull()!!
        assertEquals("Hello, world!", translation.text)
        assertEquals(Language.ENGLISH, translation.to)
        assertEquals("uk", translation.from)
    }

    @Test
    fun `test translate russian to english`() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = """{
                    "choices": [{
                        "message": {
                            "role": "assistant",
                            "content": "{\"text\": \"Hello, world!\", \"sourceLanguage\": \"ru\"}"
                        }
                    }]
                }""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    }
                )
            }
        }

        val service = QwenTranslationService(httpClient, "test-api-key")

        val result = service.translate("Привет, мир!", Language.ENGLISH)

        assertTrue(result.isSuccess)
        val translation = result.getOrNull()!!
        assertEquals("Hello, world!", translation.text)
        assertEquals(Language.ENGLISH, translation.to)
        assertEquals("ru", translation.from)
    }

    @Test
    fun `test same language translation`() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = """{
                    "choices": [{
                        "message": {
                            "role": "assistant",
                            "content": "{\"text\": \"Hello, world!\", \"sourceLanguage\": \"en\"}"
                        }
                    }]
                }""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    }
                )
            }
        }

        val service = QwenTranslationService(httpClient, "test-api-key")

        val result = service.translate("Hello, world!", Language.ENGLISH)

        assertTrue(result.isSuccess)
        val translation = result.getOrNull()!!
        assertTrue(translation.isSameLanguage())
    }

    @Test
    fun `test api error handling`() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = "Internal Server Error",
                status = HttpStatusCode.InternalServerError
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    }
                )
            }
        }

        val service = QwenTranslationService(httpClient, "test-api-key")

        val result = service.translate("Hello, world!", Language.UKRAINIAN)

        assertFalse(result.isSuccess)
    }

    @Test
    fun `test empty response handling`() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = """{
                    "choices": []
                }""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    }
                )
            }
        }

        val service = QwenTranslationService(httpClient, "test-api-key")

        val result = service.translate("Hello, world!", Language.UKRAINIAN)

        assertFalse(result.isSuccess)
    }

    @Test
    fun `test fallback parsing when AI returns non-JSON response`() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = """{
                    "choices": [{
                        "message": {
                            "role": "assistant",
                            "content": "Just the translated text without JSON"
                        }
                    }]
                }""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    }
                )
            }
        }

        val service = QwenTranslationService(httpClient, "test-api-key")

        val result = service.translate("Hello, world!", Language.UKRAINIAN)

        assertTrue(result.isSuccess)
        val translation = result.getOrNull()!!
        assertEquals("Just the translated text without JSON", translation.text)
        assertEquals(Language.UKRAINIAN, translation.to)
        assertEquals("en", translation.from) // Should fallback to simple detection
    }
}
