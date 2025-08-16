package twitterx.translation.openai

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import twitterx.translation.api.Language
import twitterx.translation.api.Translation
import twitterx.translation.api.TranslationService

public class OpenAITranslationService(
    private val httpClient: HttpClient,
    private val apiKey: String,
    private val model: String = "gpt-5-mini",
    private val baseUrl: String = "https://api.openai.com/v1"
) : TranslationService {

    private val logger = LoggerFactory.getLogger(OpenAITranslationService::class.java)
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun translate(text: String, to: Language): Result<Translation> {
        return withContext(Dispatchers.IO) {
            try {
                logger.debug("Translating text to ${to.iso} using model $model: ${text.take(50)}...")

                val prompt = buildTranslationPrompt(text, to)
                val request = ResponsesRequest(
                    model = model,
                    input = prompt,
                    text = ResponsesText(format = ResponseFormat(type = "json_object"))
                )

                val response = httpClient.post("$baseUrl/responses") {
                    headers {
                        append("Authorization", "Bearer $apiKey")
                        append("Accept", "application/json")
                    }
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }

                val responsesResponse = response.body<ResponsesResponse>()

                val responseContent = extractTextContent(responsesResponse)
                    ?: run {
                        logger.error("No output_text content in responses API output")
                        return@withContext Result.failure(Exception("Empty response from OpenAI compatible API"))
                    }

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
            You are a translation assistant. Your task is to translate the given text and identify its source language.
            You MUST return a JSON object with the following structure:
            {
              "text": "The translated text goes here.",
              "sourceLanguage": "The detected ISO 639-1 code of the source language (e.g., 'en', 'uk', 'ru')."
            }

            - Translate the following text to $targetLanguage.
            - Detect the source language of the text.
            - If the source language is the same as the target language, return the original text.
            - If the source language cannot be determined, use 'en' as the default.

            Text to translate:
            "$text"
        """.trimIndent()
    }

    private fun parseTranslationResponse(responseContent: String): TranslationResult {
        return try {
            val result = json.decodeFromString<TranslationResult>(responseContent)

            val validatedSourceLanguage = when (result.sourceLanguage.lowercase()) {
                "en", "english" -> "en"
                "uk", "ukrainian" -> "uk"
                "ru", "russian" -> "ru"
                else -> "en" // Default fallback
            }

            result.copy(sourceLanguage = validatedSourceLanguage)
        } catch (e: Exception) {
            logger.error("Failed to parse JSON response from AI: $responseContent", e)
            throw Exception("Failed to parse structured response from AI", e)
        }
    }

    private fun extractTextContent(response: ResponsesResponse): String? {
        // Find the first output item of type "message" then first content of type "output_text"
        val messageItem = response.output.firstOrNull { it.type == "message" }
        val textPiece = messageItem
            ?.content
            ?.firstOrNull { it.type == "output_text" }
            ?.text

        return textPiece?.trim()
    }
}
