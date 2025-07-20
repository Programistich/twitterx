package twitterx.libre.e2e

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import translations.libre.LibreTranslateService
import twitterx.translation.api.Language
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class LibreTranslateE2ETest {

    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                    isLenient = true
                }
            )
        }
        install(Logging) {
            level = LogLevel.INFO
        }
    }

    private val service = LibreTranslateService(httpClient)

    @Test
    fun `test api responds with error when no api key provided`() = runTest(timeout = 30.seconds) {
        val ukrainianText = "Привіт, світ!"

        val result = service.translate(ukrainianText, Language.ENGLISH)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Translation failed") == true)
    }

    /*
     * Note: The following tests are commented out because LibreTranslate.com now requires an API key.
     * To run these tests, you need to:
     * 1. Get an API key from https://portal.libretranslate.com
     * 2. Modify LibreTranslateService to accept and use the API key parameter
     * 3. Set the API key in the service configuration
     *
     * These tests verify the full E2E functionality when API access is available.
     */

    /*
    @Test
    fun `test translate ukrainian text to english`() = runTest(timeout = 30.seconds) {
        val ukrainianText = "Привіт, світ! Як справи?"

        val result = service.translate(ukrainianText, Language.ENGLISH).getOrThrow()

        assertEquals(Language.ENGLISH, result.to)
        assertEquals("uk", result.from)
        assertTrue(result.text.lowercase().contains("hello") || result.text.lowercase().contains("hi"))
        assertTrue(result.text.lowercase().contains("world"))
        assertFalse(result.isSameLanguage())
    }

    @Test
    fun `test translate ukrainian text to russian`() = runTest(timeout = 30.seconds) {
        val ukrainianText = "Слава Україні! Героям слава!"

        val result = service.translate(ukrainianText, Language.RUSSIAN).getOrThrow()

        assertEquals(Language.RUSSIAN, result.to)
        assertEquals("uk", result.from)
        assertTrue(result.text.contains("Слава") || result.text.contains("слава"))
        assertFalse(result.isSameLanguage())
    }

    @Test
    fun `test translate english text to ukrainian`() = runTest(timeout = 30.seconds) {
        val englishText = "Hello, world! How are you doing today?"

        val result = service.translate(englishText, Language.UKRAINIAN).getOrThrow()

        assertEquals(Language.UKRAINIAN, result.to)
        assertEquals("en", result.from)
        assertTrue(result.text.contains("Привіт") || result.text.contains("привіт") ||
                  result.text.contains("Здравствуй") || result.text.contains("здравствуй"))
        assertFalse(result.isSameLanguage())
    }

    @Test
    fun `test translate complex ukrainian text to english`() = runTest(timeout = 30.seconds) {
        val complexText = ""\"
            Рейнджер 4-го полку ССО, виїжджаючи на завдання, помітив російський дрон.
            Після невдалої спроби збити його з дробовика, військовий дочекався зближення дрона
            і відбив його рукою так, щоб той не здетонував.
        ""\".trimIndent()

        val result = service.translate(complexText, Language.ENGLISH).getOrThrow()

        assertEquals(Language.ENGLISH, result.to)
        assertEquals("uk", result.from)
        assertTrue(result.text.lowercase().contains("ranger") || result.text.lowercase().contains("soldier"))
        assertTrue(result.text.lowercase().contains("drone"))
        assertFalse(result.isSameLanguage())
    }

    @Test
    fun `test translate same language detection`() = runTest(timeout = 30.seconds) {
        val englishText = "Hello, world!"

        val result = service.translate(englishText, Language.ENGLISH).getOrThrow()

        assertEquals(Language.ENGLISH, result.to)
        assertEquals("en", result.from)
        assertTrue(result.isSameLanguage())
        // LibreTranslate might return the same text or a slight variation
        assertTrue(result.text.lowercase().contains("hello"))
    }

    @Test
    fun `test translate russian text to ukrainian`() = runTest(timeout = 30.seconds) {
        val russianText = "Добро пожаловать в наш город!"

        val result = service.translate(russianText, Language.UKRAINIAN).getOrThrow()

        assertEquals(Language.UKRAINIAN, result.to)
        assertEquals("ru", result.from)
        assertTrue(result.text.contains("Ласкаво") || result.text.contains("ласкаво") ||
                  result.text.contains("Добро") || result.text.contains("добро"))
        assertFalse(result.isSameLanguage())
    }

    @Test
    fun `test translate short text`() = runTest(timeout = 30.seconds) {
        val shortText = "Так"

        val result = service.translate(shortText, Language.ENGLISH).getOrThrow()

        assertEquals(Language.ENGLISH, result.to)
        assertTrue(result.from == "uk" || result.from == "ru") // Could be detected as either
        assertTrue(result.text.lowercase() == "yes" || result.text.lowercase() == "so" ||
                  result.text.lowercase() == "thus")
        assertFalse(result.isSameLanguage())
    }

    @Test
    fun `test translate technical text`() = runTest(timeout = 30.seconds) {
        val technicalText = "Машинне навчання та штучний інтелект змінюють світ технологій."

        val result = service.translate(technicalText, Language.ENGLISH).getOrThrow()

        assertEquals(Language.ENGLISH, result.to)
        assertEquals("uk", result.from)
        assertTrue(result.text.lowercase().contains("machine") || result.text.lowercase().contains("learning"))
        assertTrue(result.text.lowercase().contains("artificial") || result.text.lowercase().contains("intelligence"))
        assertFalse(result.isSameLanguage())
    }
     */
}
