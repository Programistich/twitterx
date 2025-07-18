package twitterx.qween.e2e

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import twitter.translations.qween.QwenTranslationService
import twitterx.translation.api.Language
import kotlin.test.Test
import kotlin.test.assertTrue

class QwenE2ETest {

    private val httpClient: HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                }
            )
        }
    }

    private val apiKey = System.getenv("OPENROUTER_API_KEY") ?: "test-api-key"
    private val service = QwenTranslationService(httpClient, apiKey)

    @Test
    fun `test translate from english to ukrainian`() = runTest {
        // Skip test if no API key provided
        if (apiKey == "test-api-key") {
            println("Skipping E2E test - no OPENROUTER_API_KEY environment variable")
            return@runTest
        }

        val result = service.translate("Hello, world!", Language.UKRAINIAN).getOrThrow()
        assertTrue(result.from == "en")
        assertTrue(result.to.iso == Language.UKRAINIAN.iso)
        assertTrue(result.text.isNotBlank())
        println("Translated: '${result.text}'")
    }

    @Test
    fun `test translate from ukrainian to english`() = runTest {
        // Skip test if no API key provided
        if (apiKey == "test-api-key") {
            println("Skipping E2E test - no OPENROUTER_API_KEY environment variable")
            return@runTest
        }

        val result = service.translate("Привіт, світ!", Language.ENGLISH).getOrThrow()
        assertTrue(result.from == "uk")
        assertTrue(result.to.iso == Language.ENGLISH.iso)
        assertTrue(result.text.isNotBlank())
        println("Translated: '${result.text}'")
    }

    @Test
    fun `test translate same language`() = runTest {
        // Skip test if no API key provided
        if (apiKey == "test-api-key") {
            println("Skipping E2E test - no OPENROUTER_API_KEY environment variable")
            return@runTest
        }

        val result = service.translate("Hello, world!", Language.ENGLISH).getOrThrow()
        assertTrue(result.isSameLanguage())
        println("Same language translation: '${result.text}'")
    }
}
