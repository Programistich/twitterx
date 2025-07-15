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
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GoogleTranslationServiceTest {

    private fun createMockClient(responseJson: String): HttpClient {
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
    fun `translate from English to Ukrainian should return correct translation`() = runTest {
        val mockResponse = GoogleTranslateResponse(
            sentences = listOf(
                GoogleTranslateResponse.Sentence(
                    text = "Привіт світе",
                    from = "en"
                )
            ),
            src = "en"
        )
        val responseJson = Json.encodeToString(GoogleTranslateResponse.serializer(), mockResponse)
        val httpClient = createMockClient(responseJson)
        val service = GoogleTranslationService(httpClient)

        val result = service.translate("Hello world", Language.UKRAINIAN)

        assertTrue(result.isSuccess)
        val translation = result.getOrThrow()
        assertEquals("Привіт світе", translation.text)
        assertEquals("en", translation.from)
        assertEquals(Language.UKRAINIAN, translation.to)
    }

    @Test
    fun `translate from Ukrainian to English should return correct translation`() = runTest {
        val mockResponse = GoogleTranslateResponse(
            sentences = listOf(
                GoogleTranslateResponse.Sentence(
                    text = "Hello world",
                    from = "uk"
                )
            ),
            src = "uk"
        )
        val responseJson = Json.encodeToString(GoogleTranslateResponse.serializer(), mockResponse)
        val httpClient = createMockClient(responseJson)
        val service = GoogleTranslationService(httpClient)

        val result = service.translate("Привіт світе", Language.ENGLISH)

        assertTrue(result.isSuccess)
        val translation = result.getOrThrow()
        assertEquals("Hello world", translation.text)
        assertEquals("uk", translation.from)
        assertEquals(Language.ENGLISH, translation.to)
    }

    @Test
    fun `translate from Russian to English should return correct translation`() = runTest {
        val mockResponse = GoogleTranslateResponse(
            sentences = listOf(
                GoogleTranslateResponse.Sentence(
                    text = "Hello world",
                    from = "ru"
                )
            ),
            src = "ru"
        )
        val responseJson = Json.encodeToString(GoogleTranslateResponse.serializer(), mockResponse)
        val httpClient = createMockClient(responseJson)
        val service = GoogleTranslationService(httpClient)

        val result = service.translate("Привет мир", Language.ENGLISH)

        assertTrue(result.isSuccess)
        val translation = result.getOrThrow()
        assertEquals("Hello world", translation.text)
        assertEquals("ru", translation.from)
        assertEquals(Language.ENGLISH, translation.to)
    }

    @Test
    fun `translate from English to Russian should return correct translation`() = runTest {
        val mockResponse = GoogleTranslateResponse(
            sentences = listOf(
                GoogleTranslateResponse.Sentence(
                    text = "Привет мир",
                    from = "en"
                )
            ),
            src = "en"
        )
        val responseJson = Json.encodeToString(GoogleTranslateResponse.serializer(), mockResponse)
        val httpClient = createMockClient(responseJson)
        val service = GoogleTranslationService(httpClient)

        val result = service.translate("Hello world", Language.RUSSIAN)

        assertTrue(result.isSuccess)
        val translation = result.getOrThrow()
        assertEquals("Привет мир", translation.text)
        assertEquals("en", translation.from)
        assertEquals(Language.RUSSIAN, translation.to)
    }

    @Test
    fun `translate same language should detect source language correctly`() = runTest {
        val mockResponse = GoogleTranslateResponse(
            sentences = listOf(
                GoogleTranslateResponse.Sentence(
                    text = "Hello world",
                    from = "en"
                )
            ),
            src = "en"
        )
        val responseJson = Json.encodeToString(GoogleTranslateResponse.serializer(), mockResponse)
        val httpClient = createMockClient(responseJson)
        val service = GoogleTranslationService(httpClient)

        val result = service.translate("Hello world", Language.ENGLISH)

        assertTrue(result.isSuccess)
        val translation = result.getOrThrow()
        assertEquals("Hello world", translation.text)
        assertEquals("en", translation.from)
        assertEquals(Language.ENGLISH, translation.to)
        assertTrue(translation.isSameLanguage())
    }

    @Test
    fun `translate empty string should return empty translation`() = runTest {
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
        val httpClient = createMockClient(responseJson)
        val service = GoogleTranslationService(httpClient)

        val result = service.translate("", Language.UKRAINIAN)

        assertTrue(result.isSuccess)
        val translation = result.getOrThrow()
        assertEquals("", translation.text)
        assertEquals("en", translation.from)
        assertEquals(Language.UKRAINIAN, translation.to)
    }

    @Test
    fun `translate multiline text should preserve formatting`() = runTest {
        val originalText = "Line 1\nLine 2\nLine 3"
        val translatedText = "Рядок 1\nРядок 2\nРядок 3"

        val mockResponse = GoogleTranslateResponse(
            sentences = listOf(
                GoogleTranslateResponse.Sentence(
                    text = translatedText,
                    from = "en"
                )
            ),
            src = "en"
        )
        val responseJson = Json.encodeToString(GoogleTranslateResponse.serializer(), mockResponse)
        val httpClient = createMockClient(responseJson)
        val service = GoogleTranslationService(httpClient)

        val result = service.translate(originalText, Language.UKRAINIAN)

        assertTrue(result.isSuccess)
        val translation = result.getOrThrow()
        assertEquals(translatedText, translation.text)
        assertEquals("en", translation.from)
        assertEquals(Language.UKRAINIAN, translation.to)
    }

    @Test
    fun `translate text with special characters should handle them correctly`() = runTest {
        val originalText = "Hello @user! Check this: https://example.com #hashtag"
        val translatedText = "Привіт @user! Перевір це: https://example.com #hashtag"

        val mockResponse = GoogleTranslateResponse(
            sentences = listOf(
                GoogleTranslateResponse.Sentence(
                    text = translatedText,
                    from = "en"
                )
            ),
            src = "en"
        )
        val responseJson = Json.encodeToString(GoogleTranslateResponse.serializer(), mockResponse)
        val httpClient = createMockClient(responseJson)
        val service = GoogleTranslationService(httpClient)

        val result = service.translate(originalText, Language.UKRAINIAN)

        assertTrue(result.isSuccess)
        val translation = result.getOrThrow()
        assertEquals(translatedText, translation.text)
        assertEquals("en", translation.from)
        assertEquals(Language.UKRAINIAN, translation.to)
    }

    @Test
    fun `translate with emoji should preserve emoji`() = runTest {
        val originalText = "Hello world! 😀🌍"
        val translatedText = "Привіт світе! 😀🌍"

        val mockResponse = GoogleTranslateResponse(
            sentences = listOf(
                GoogleTranslateResponse.Sentence(
                    text = translatedText,
                    from = "en"
                )
            ),
            src = "en"
        )
        val responseJson = Json.encodeToString(GoogleTranslateResponse.serializer(), mockResponse)
        val httpClient = createMockClient(responseJson)
        val service = GoogleTranslationService(httpClient)

        val result = service.translate(originalText, Language.UKRAINIAN)

        assertTrue(result.isSuccess)
        val translation = result.getOrThrow()
        assertEquals(translatedText, translation.text)
        assertEquals("en", translation.from)
        assertEquals(Language.UKRAINIAN, translation.to)
    }

    @Test
    fun `translate long text should handle it correctly`() = runTest {
        val originalText = "This is a very long text that should be translated properly. ".repeat(10)
        val translatedText = "Це дуже довгий текст, який має бути правильно перекладений. ".repeat(10)

        val mockResponse = GoogleTranslateResponse(
            sentences = listOf(
                GoogleTranslateResponse.Sentence(
                    text = translatedText,
                    from = "en"
                )
            ),
            src = "en"
        )
        val responseJson = Json.encodeToString(GoogleTranslateResponse.serializer(), mockResponse)
        val httpClient = createMockClient(responseJson)
        val service = GoogleTranslationService(httpClient)

        val result = service.translate(originalText, Language.UKRAINIAN)

        assertTrue(result.isSuccess)
        val translation = result.getOrThrow()
        assertEquals(translatedText, translation.text)
        assertEquals("en", translation.from)
        assertEquals(Language.UKRAINIAN, translation.to)
    }
}
