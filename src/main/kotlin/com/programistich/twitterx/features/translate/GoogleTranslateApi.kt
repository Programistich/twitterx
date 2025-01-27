package com.programistich.twitterx.features.translate

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.springframework.stereotype.Component

@Component
class GoogleTranslateApi(
    private val httpClient: HttpClient
) {
    companion object {
        private const val BASE_URL = "https://translate.google.com/translate_a/single"
    }

    suspend fun translate(text: String, to: String): GoogleTranslateResult {
        return try {
            val response = httpClient.get(BASE_URL) {
                url {
                    parameters.append("client", "gtx")
                    parameters.append("sl", "auto")
                    parameters.append("tl", to)
                    parameters.append("dt", "t")
                    parameters.append("dt", "bd")
                    parameters.append("dj", "1")
                    parameters.append("q", text)
                }
            }.body<GoogleTranslateResponse>()

            val result = response.sentences.firstOrNull()
                ?: return GoogleTranslateResult.Error(GoogleTranslateResult.ErrorType.EMPTY_RESPONSE)

            return GoogleTranslateResult.Translated(from = result.orig, to = result.trans)
        } catch (e: Exception) {
            GoogleTranslateResult.Error(GoogleTranslateResult.ErrorType.UNKNOWN)
        }
    }
}
