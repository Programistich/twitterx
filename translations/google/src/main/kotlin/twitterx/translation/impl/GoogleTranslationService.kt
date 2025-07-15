package twitterx.translation.impl

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import twitterx.translation.api.Language
import twitterx.translation.api.Translation
import twitterx.translation.api.TranslationService

public class GoogleTranslationService(
    private val httpClient: HttpClient
) : TranslationService {
    private companion object {
        private const val BASE_URL = "https://translate.google.com/translate_a/single"
        private val logger: Logger = LoggerFactory.getLogger(GoogleTranslationService::class.java)
    }

    override suspend fun translate(
        text: String,
        to: Language,
    ): Result<Translation> = runCatching {
        logger.debug("Starting translation request: text length={}, target language={}", text.length, to.iso)

        val response = httpClient.get(BASE_URL) {
            url {
                parameters.append("client", "gtx")
                parameters.append("sl", "auto") // Automatically detect source language
                parameters.append("tl", to.iso)
                parameters.append("dt", "t")
                parameters.append("dt", "bd")
                parameters.append("dj", "1")
                parameters.append("q", text)
            }
        }.body<GoogleTranslateResponse>()

        val text = response.sentences.joinToString("") { it.text }
        val translation = Translation(
            from = response.src,
            to = to,
            text = text
        )

        logger.info("Translation completed successfully: {} -> {}", response.src, to.iso)
        logger.debug("Translation result: original length={}, translated length={}", text.length, text)

        return Result.success(translation)
    }.onFailure { exception ->
        logger.error("Translation failed: text length={}, target language={}", text.length, to.iso, exception)
    }
}
