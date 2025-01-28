package com.programistich.twitterx.core.telegraph

import com.programistich.twitterx.core.telegram.models.TelegramConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.springframework.stereotype.Component

@Component
class TelegraphApi(
    private val httpClient: HttpClient,
    private val telegramConfig: TelegramConfig
) {
    @Volatile
    private var cacheToken: String? = null

    suspend fun createPage(title: String, content: String): Result<String> {
        return runCatching {
            val token = cacheToken ?: run {
                val newToken = createAccount()
                cacheToken = newToken
                return@run newToken
            }

            val createdPage = createPage(
                token = token,
                title = title,
                content = content
            )

            createdPage.url
        }
    }

    private suspend fun createAccount(): String {
        val body = CreateAccountRequest(
            shortName = telegramConfig.botUsername,
            authorName = telegramConfig.botUsername,
            authorUrl = "https://t.me/@${telegramConfig.botUsername}"
        )

        val response = httpClient.post("https://api.telegra.ph/createAccount") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body<ApiResponse<CreateAccountResponse>>()

        if (!response.ok || response.result == null) {
            throw TelegraphCreateAccountException(
                "Failed to create Telegraph account. " + "Error: ${response.error ?: "Unknown error"}"
            )
        }

        return response.result.accessToken
    }

    private suspend fun createPage(
        token: String,
        title: String,
        content: String
    ): CreatePageResponse {
        val paragraphs = content
            .split("\n\n")
            .map { NodeElement(tag = "p", children = listOf(it)) }

        val body = CreatePageRequest(
            accessToken = token,
            title = title,
            authorName = telegramConfig.botUsername,
            content = paragraphs
        )

        val response = httpClient.post("https://api.telegra.ph/createPage") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body<ApiResponse<CreatePageResponse>>()

        if (!response.ok || response.result == null) {
            throw TelegraphCreatePageException(
                "Failed to create Telegraph page. " + "Error: ${response.error ?: "Unknown error"}"
            )
        }

        return response.result
    }
}
