package twitterx.translation.impl

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import twitterx.translation.api.Language
import kotlin.test.Test
import kotlin.test.assertTrue

class GoogleTranslationServiceErrorTest {

    private fun createMockClientWithError(statusCode: HttpStatusCode): HttpClient {
        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel("Error response"),
                status = statusCode,
                headers = headersOf(HttpHeaders.ContentType, "text/plain")
            )
        }
        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }
    }

    private fun createMockClientWithInvalidJson(): HttpClient {
        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel("Invalid JSON"),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }
    }

    private fun createMockClientWithEmptyResponse(): HttpClient {
        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel(""),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }
    }

    private fun createMockClientWithEmptySentences(): HttpClient {
        val mockResponse = GoogleTranslateResponse(sentences = emptyList(), src = "en")
        val responseJson = Json.encodeToString(GoogleTranslateResponse.serializer(), mockResponse)

        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel(responseJson),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }
    }

    @Test
    fun `translate should handle HTTP 400 error`() = runTest {
        val httpClient = createMockClientWithError(HttpStatusCode.BadRequest)
        val service = GoogleTranslationService(httpClient)

        val result = service.translate("Hello world", Language.UKRAINIAN)

        assertTrue(result.isFailure)
    }

    @Test
    fun `translate should handle HTTP 500 error`() = runTest {
        val httpClient = createMockClientWithError(HttpStatusCode.InternalServerError)
        val service = GoogleTranslationService(httpClient)

        val result = service.translate("Hello world", Language.UKRAINIAN)

        assertTrue(result.isFailure)
    }

    @Test
    fun `translate should handle HTTP 503 error`() = runTest {
        val httpClient = createMockClientWithError(HttpStatusCode.ServiceUnavailable)
        val service = GoogleTranslationService(httpClient)

        val result = service.translate("Hello world", Language.UKRAINIAN)

        assertTrue(result.isFailure)
    }

    @Test
    fun `translate should handle network timeout`() = runTest {
        val mockEngine = MockEngine { request ->
            throw IllegalStateException("Network timeout")
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }
        val service = GoogleTranslationService(httpClient)

        val result = service.translate("Hello world", Language.UKRAINIAN)

        assertTrue(result.isFailure)
    }

    @Test
    fun `translate should handle invalid JSON response`() = runTest {
        val httpClient = createMockClientWithInvalidJson()
        val service = GoogleTranslationService(httpClient)

        val result = service.translate("Hello world", Language.UKRAINIAN)

        assertTrue(result.isFailure)
    }

    @Test
    fun `translate should handle empty response`() = runTest {
        val httpClient = createMockClientWithEmptyResponse()
        val service = GoogleTranslationService(httpClient)

        val result = service.translate("Hello world", Language.UKRAINIAN)

        assertTrue(result.isFailure)
    }

    @Test
    fun `translate should handle empty sentences list`() = runTest {
        val httpClient = createMockClientWithEmptySentences()
        val service = GoogleTranslationService(httpClient)

        val result = service.translate("Hello world", Language.UKRAINIAN)

        assertTrue(result.isFailure)
    }

    @Test
    fun `translate should handle null text input`() = runTest {
        val mockResponse = GoogleTranslateResponse(
            sentences = listOf(
                GoogleTranslateResponse.Sentence(
                    text = "",
                    from = "en"
                )
            ),
            src = "en"
        )
        val responseJson = Json.encodeToString(GoogleTranslateResponse.serializer(), mockResponse)
        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel(responseJson),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }
        val service = GoogleTranslationService(httpClient)

        val result = service.translate("", Language.UKRAINIAN)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `translate should handle malformed locale codes`() = runTest {
        val mockResponse = GoogleTranslateResponse(
            sentences = listOf(
                GoogleTranslateResponse.Sentence(
                    text = "Test translation",
                    from = "invalid-locale"
                )
            ),
            src = "invalid-locale"
        )
        val responseJson = Json.encodeToString(GoogleTranslateResponse.serializer(), mockResponse)
        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel(responseJson),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }
        val service = GoogleTranslationService(httpClient)

        val result = service.translate("Test text", Language.UKRAINIAN)

        assertTrue(result.isSuccess)
        val translation = result.getOrThrow()
        assertTrue(translation.from == "invalid-locale" || translation.from.isEmpty())
    }

    @Test
    fun `translate should handle very long text input`() = runTest {
        val longText = "a".repeat(10000)
        val mockResponse = GoogleTranslateResponse(
            sentences = listOf(
                GoogleTranslateResponse.Sentence(
                    text = "б".repeat(10000),
                    from = "en"
                )
            ),
            src = "en"
        )
        val responseJson = Json.encodeToString(GoogleTranslateResponse.serializer(), mockResponse)
        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel(responseJson),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }
        val service = GoogleTranslationService(httpClient)

        val result = service.translate(longText, Language.UKRAINIAN)

        assertTrue(result.isSuccess)
        val translation = result.getOrThrow()
        assertTrue(translation.text.length > 5000)
    }

    @Test
    fun `translate should handle special Unicode characters`() = runTest {
        val specialText = "𝓗𝓮𝓵𝓵𝓸 𝕨𝕠𝕣𝕝𝕕 🌍💫✨"
        val mockResponse = GoogleTranslateResponse(
            sentences = listOf(
                GoogleTranslateResponse.Sentence(
                    text = "𝓟𝓻𝓲𝓿𝓲𝓽 𝓼𝓿𝓲𝓽𝓮 🌍💫✨",
                    from = "en"
                )
            ),
            src = "en"
        )
        val responseJson = Json.encodeToString(GoogleTranslateResponse.serializer(), mockResponse)
        val mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel(responseJson),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }
        val service = GoogleTranslationService(httpClient)

        val result = service.translate(specialText, Language.UKRAINIAN)

        assertTrue(result.isSuccess)
        val translation = result.getOrThrow()
        assertTrue(translation.text.contains("🌍💫✨"))
    }
}
