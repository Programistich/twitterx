package twitterx.google.e2e

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import twitterx.translation.api.Language
import twitterx.translation.impl.GoogleTranslationService
import kotlin.test.Test
import kotlin.test.assertTrue

class GoogleE2ETest {

    val httpClient: HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                }
            )
        }
    }
    val service = GoogleTranslationService(httpClient)

    @Test
    fun `test translate from uk to en`() = runTest {
        val result = service.translate("Hello, world!", Language.UKRAINIAN).getOrThrow()
        assertTrue(result.from == "en")
        assertTrue(result.to.iso == Language.UKRAINIAN.iso)
        assertTrue(result.text == "Привіт, світ!")
    }

    @Test
    fun `test translate same`() = runTest {
        val result = service.translate("Hello, world!", Language.ENGLISH).getOrThrow()
        assertTrue(result.isSameLanguage())
    }
}
