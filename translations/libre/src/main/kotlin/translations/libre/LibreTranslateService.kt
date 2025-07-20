package translations.libre

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.slf4j.LoggerFactory
import twitterx.translation.api.Language
import twitterx.translation.api.Translation
import twitterx.translation.api.TranslationService

public class LibreTranslateService(
    private val httpClient: HttpClient,
    private val baseUrl: String = "https://libretranslate.com"
) : TranslationService {

    private val logger = LoggerFactory.getLogger(LibreTranslateService::class.java)

    public override suspend fun translate(
        text: String,
        to: Language,
    ): Result<Translation> {
        return try {
            logger.debug("Translating text to ${to.iso}: ${text.take(100)}...")

            val request = LibreTranslateRequest(
                q = text,
                source = "auto",
                target = to.iso,
                format = "text",
                alternatives = 3,
                api_key = ""
            )

            val response: LibreTranslateResponse = httpClient.post("$baseUrl/translate") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

            val translation = Translation(
                text = response.translatedText,
                to = to,
                from = response.detectedLanguage.language
            )

            logger.debug("Translation successful: from=${translation.from} to=${translation.to.iso}")

            Result.success(translation)
        } catch (e: ClientRequestException) {
            logger.error("Client error during translation: ${e.message}", e)
            Result.failure(RuntimeException("Translation request failed: ${e.message}", e))
        } catch (e: ServerResponseException) {
            logger.error("Server error during translation: ${e.message}", e)
            Result.failure(RuntimeException("Translation server error: ${e.message}", e))
        } catch (e: HttpRequestTimeoutException) {
            logger.error("Timeout during translation: ${e.message}", e)
            Result.failure(RuntimeException("Translation request timeout", e))
        } catch (e: Exception) {
            logger.error("Unexpected error during translation: ${e.message}", e)
            Result.failure(RuntimeException("Translation failed: ${e.message}", e))
        }
    }
}
