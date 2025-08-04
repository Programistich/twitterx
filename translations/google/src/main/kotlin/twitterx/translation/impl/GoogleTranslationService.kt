package twitterx.translation.impl

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.contentType
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
        logger.debug(
            "Starting translation request: text length={}, target language={}",
            text.length,
            to.iso
        )

        val response = httpClient.post(BASE_URL) {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(
                FormDataContent(
                    Parameters.build {
                        append("client", "it")
                        append("dt", "qca")
                        append("dt", "t")
                        append("dt", "rmt")
                        append("dt", "bd")
                        append("dt", "rms")
                        append("dt", "sos")
                        append("dt", "md")
                        append("dt", "gt")
                        append("dt", "ld")
                        append("dt", "ss")
                        append("dt", "ex")
                        append("otf", "2")
                        append("dj", "1")
                        append("hl", "en")
                        append("ie", "UTF-8")
                        append("oe", "UTF-8")
                        append("sl", "auto")
                        append("tl", to.iso)
                        append("q", text)
                    }
                )
            )
        }.body<GoogleTranslateResponse>()

        val translatedText = response.sentences.joinToString("") { it.text }
        val translation = Translation(
            from = response.src,
            to = to,
            text = translatedText
        )

        logger.info("Translation completed successfully: {} -> {}", response.src, to.iso)
        logger.debug(
            "Translation result: original length={}, translated length={}",
            text.length,
            translatedText.length
        )
        translation
    }
}
