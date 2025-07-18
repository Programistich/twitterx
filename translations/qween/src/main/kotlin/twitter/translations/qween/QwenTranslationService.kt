package twitter.translations.qween

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import twitterx.translation.api.Language
import twitterx.translation.api.Translation
import twitterx.translation.api.TranslationService

public class QwenTranslationService(
    private val httpClient: HttpClient,
    private val apiKey: String,
    private val baseUrl: String = "https://openrouter.ai/api/v1"
) : TranslationService {

    private val logger = LoggerFactory.getLogger(QwenTranslationService::class.java)

    override suspend fun translate(text: String, to: Language): Result<Translation> {
        return withContext(Dispatchers.IO) {
            try {
                logger.debug("Translating text to ${to.iso}: $text")

                val prompt = buildTranslationPrompt(text, to)
                val request = OpenRouterRequest(
                    model = "qwen/qwen3-30b-a3b:free",
                    messages = listOf(
                        OpenRouterMessage(
                            role = "user",
                            content = prompt
                        )
                    )
                )

                val response = httpClient.post("$baseUrl/chat/completions") {
                    headers {
                        append("Authorization", "Bearer $apiKey")
                    }
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }

                val openRouterResponse = response.body<OpenRouterResponse>()

                if (openRouterResponse.choices.isEmpty()) {
                    logger.error("Empty choices in OpenRouter response")
                    return@withContext Result.failure(Exception("Empty response from OpenRouter"))
                }

                val responseContent = openRouterResponse.choices.first().message.content.trim()
                val translationResult = parseTranslationResponse(responseContent)

                val translation = Translation(
                    text = translationResult.text,
                    to = to,
                    from = translationResult.sourceLanguage
                )

                logger.debug("Translation successful: $translation")
                Result.success(translation)
            } catch (e: Exception) {
                logger.error("Translation failed", e)
                Result.failure(e)
            }
        }
    }

    private fun buildTranslationPrompt(text: String, to: Language): String {
        val targetLanguage = when (to) {
            Language.ENGLISH -> "English"
            Language.UKRAINIAN -> "Ukrainian"
            Language.RUSSIAN -> "Russian"
        }

        return """
            Translate the following text to $targetLanguage and detect the source language.
            
            Return your response as a JSON object with this exact format:
            {
                "text": "translated text here",
                "sourceLanguage": "detected source language code (en, uk, ru)"
            }
            
            Important:
            - Only return the JSON object, no additional text
            - Use language codes: "en" for English, "uk" for Ukrainian, "ru" for Russian
            - If you cannot detect the source language, use "en" as default
            
            Text to translate:
            $text
        """.trimIndent()
    }

    private fun parseTranslationResponse(responseContent: String): TranslationResult {
        return try {
            // Try to parse as JSON first
            val json = Json { ignoreUnknownKeys = true }
            val result = json.decodeFromString<TranslationResult>(responseContent)

            // Validate the source language code
            val validatedSourceLanguage = when (result.sourceLanguage.lowercase()) {
                "en", "english" -> "en"
                "uk", "ukrainian" -> "uk"
                "ru", "russian" -> "ru"
                else -> "en" // Default fallback
            }

            result.copy(sourceLanguage = validatedSourceLanguage)
        } catch (e: Exception) {
            logger.warn("Failed to parse JSON response, falling back to simple detection: $responseContent", e)
            // Fallback to simple detection if JSON parsing fails
            TranslationResult(
                text = responseContent,
                sourceLanguage = detectSourceLanguageFallback(responseContent)
            )
        }
    }

    private fun detectSourceLanguageFallback(text: String): String {
        // Simple language detection based on character analysis as fallback
        val cyrillicCount = text.count { it in 'а'..'я' || it in 'А'..'Я' }
        val ukrainianChars = text.count { it in "іїєґІЇЄҐ" }
        val totalChars = text.length

        return when {
            cyrillicCount.toDouble() / totalChars > 0.5 -> {
                if (ukrainianChars > 0) "uk" else "ru"
            }
            else -> "en"
        }
    }
}

@Serializable
internal data class OpenRouterRequest(
    val model: String,
    val messages: List<OpenRouterMessage>
)

@Serializable
internal data class OpenRouterMessage(
    val role: String,
    val content: String
)

@Serializable
internal data class OpenRouterResponse(
    val choices: List<OpenRouterChoice>
)

@Serializable
internal data class OpenRouterChoice(
    val message: OpenRouterMessage
)

@Serializable
internal data class TranslationResult(
    val text: String,
    val sourceLanguage: String
)
