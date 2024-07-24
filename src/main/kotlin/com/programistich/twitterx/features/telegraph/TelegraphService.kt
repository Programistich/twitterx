package com.programistich.twitterx.features.telegraph

import com.programistich.twitterx.twitter.service.Author
import com.programistich.twitterx.twitter.service.Tweet
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Component

@Component
class TelegraphService {
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                }
            )
        }
    }

    suspend fun getPageUrl(text: String, tweet: Tweet): String {
        val createAccount = createAccount(tweet.author)
        val createPage = createPage(createAccount, tweet, text)
        return createPage.url
    }

    private suspend fun createAccount(author: Author): CreateAccountResponse {
        val body = CreateAccountRequest(
            shortName = author.name,
            authorName = author.username,
            authorUrl = author.url
        )

        val response = httpClient.post("https://api.telegra.ph/createAccount") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body<ApiResponse<CreateAccountResponse>>()

        if (!response.ok) throw TelegraphCreateAccountException()
        return response.result!!
    }

    private suspend fun createPage(account: CreateAccountResponse, tweet: Tweet, text: String): CreatePageResponse {
        val paragraphs = text.split("\n\n").map { NodeElement("p", listOf(it)) }
        val body = CreatePageRequest(
            accessToken = account.accessToken,
            title = "Tweet ${tweet.id} by ${tweet.author.name}",
            authorName = account.authorName,
            content = paragraphs
        )

        val response = httpClient.post("https://api.telegra.ph/createPage") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body<ApiResponse<CreatePageResponse>>()

        if (!response.ok) throw TelegraphCreatePageException()
        return response.result!!
    }
}
