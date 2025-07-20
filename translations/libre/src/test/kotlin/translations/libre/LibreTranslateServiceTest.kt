package translations.libre

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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import twitterx.translation.api.Language
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LibreTranslateServiceTest {

    private fun createMockClient(responseJson: String, status: HttpStatusCode = HttpStatusCode.OK): HttpClient {
        return HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    respond(
                        content = responseJson,
                        status = status,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    )
                }
            }
            install(ContentNegotiation) {
                json()
            }
        }
    }

    @Test
    fun `test translate from ukrainian to english`() = runTest {
        val responseJson = Json.encodeToString(
            LibreTranslateResponse(
                translatedText = "Ranger of the 4th SSO regiment, going on a mission, noticed a Russian drone. After an unsuccessful attempt to shoot it down with a shotgun, the military waited for the drone to approach and deflected it with his hand so that it would not detonate.",
                detectedLanguage = DetectedLanguage(confidence = 100, language = "uk"),
                alternatives = listOf(
                    "Ranger of the 4th SSO regiment, going on a mission, noticed a Russian drone.",
                    "Ranger of the 4th regiment, going on assignment, noticed Russian drone.",
                    "4th SSO regiment ranger, going on mission, spotted Russian drone."
                )
            )
        )

        val httpClient = createMockClient(responseJson)
        val service = LibreTranslateService(httpClient)

        val result = service.translate(
            "Рейнджер 4-го полку ССО, виїжджаючи на завдання, помітив російський дрон. Після невдалої спроби збити його з дробовика, військовий дочекався зближення дрона і відбив його рукою так, щоб той не здетонував.",
            Language.ENGLISH
        ).getOrThrow()

        assertEquals("uk", result.from)
        assertEquals(Language.ENGLISH, result.to)
        assertTrue(result.text.contains("Ranger"))
        assertTrue(result.text.contains("drone"))
        assertFalse(result.isSameLanguage())
    }

    @Test
    fun `test translate from ukrainian to russian`() = runTest {
        val responseJson = Json.encodeToString(
            LibreTranslateResponse(
                translatedText = "Рейнджер 4-го полка ССО, выезжая на задание, заметил российский дрон. После неудачной попытки сбить его дробовиком, военный дождался сближения дрона и отбил его рукой так, чтобы тот не детонировал.",
                detectedLanguage = DetectedLanguage(confidence = 100, language = "uk"),
                alternatives = listOf(
                    "Рейнджер 4-го полка ССО, выезжая на задание, заметил российский дрон.",
                    "Боец 4-го полка ССО, отправляясь на задание, заметил российский дрон.",
                    "Военный 4-го полка ССО, выехав на задание, увидел российский дрон."
                )
            )
        )

        val httpClient = createMockClient(responseJson)
        val service = LibreTranslateService(httpClient)

        val result = service.translate(
            "Рейнджер 4-го полку ССО, виїжджаючи на завдання, помітив російський дрон. Після невдалої спроби збити його з дробовика, військовий дочекався зближення дрона і відбив його рукою так, щоб той не здетонував.",
            Language.RUSSIAN
        ).getOrThrow()

        assertEquals("uk", result.from)
        assertEquals(Language.RUSSIAN, result.to)
        assertTrue(result.text.contains("Рейнджер"))
        assertTrue(result.text.contains("дрон"))
        assertFalse(result.isSameLanguage())
    }

    @Test
    fun `test translate same language detection`() = runTest {
        val responseJson = Json.encodeToString(
            LibreTranslateResponse(
                translatedText = "Hello, world!",
                detectedLanguage = DetectedLanguage(confidence = 100, language = "en"),
                alternatives = listOf("Hello, world!", "Hi, world!", "Greetings, world!")
            )
        )

        val httpClient = createMockClient(responseJson)
        val service = LibreTranslateService(httpClient)

        val result = service.translate("Hello, world!", Language.ENGLISH).getOrThrow()

        assertEquals("en", result.from)
        assertEquals(Language.ENGLISH, result.to)
        assertEquals("Hello, world!", result.text)
        assertTrue(result.isSameLanguage())
    }

    @Test
    fun `test translate handles server error`() = runTest {
        val httpClient = HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    respond(
                        content = "Internal Server Error",
                        status = HttpStatusCode.InternalServerError,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Text.Plain.toString())
                    )
                }
            }
            install(ContentNegotiation) {
                json()
            }
        }
        val service = LibreTranslateService(httpClient)

        val result = service.translate("Test text", Language.ENGLISH)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Translation failed") == true)
    }

    @Test
    fun `test translate handles client error`() = runTest {
        val httpClient = HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    respond(
                        content = "Bad Request",
                        status = HttpStatusCode.BadRequest,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Text.Plain.toString())
                    )
                }
            }
            install(ContentNegotiation) {
                json()
            }
        }
        val service = LibreTranslateService(httpClient)

        val result = service.translate("Test text", Language.ENGLISH)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Translation failed") == true)
    }
}
