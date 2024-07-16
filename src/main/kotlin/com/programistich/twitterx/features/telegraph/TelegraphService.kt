package com.programistich.twitterx.features.telegraph

import com.programistich.twitterx.twitter.service.Tweet
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
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

    suspend fun getPage(
        text: String,
        tweet: Tweet
    ): String {
        val author = tweet.author
        val account = createAccount(
            shortName = author.username,
            authorName = author.username,
            authorUrl = author.url
        ) ?: throw Exception("When create account")

        return createPage(
            account = account,
            text = text,
            tweet = tweet
        )?.url ?: throw Exception("When create page")
    }

    private suspend fun createAccount(
        shortName: String,
        authorName: String,
        authorUrl: String
    ): Account? {
        val response: ApiResponse<Account> = httpClient.post("https://api.telegra.ph/createAccount") {
            contentType(ContentType.Application.Json)
            parameter("short_name", shortName)
            parameter("author_name", authorName)
            parameter("author_url", authorUrl)
        }.body()
        return response.result
    }

    private suspend fun createPage(
        account: Account,
        tweet: Tweet,
        text: String,
    ): Page? {
        val title = tweet.id
        val nodes = text.split("\n").map { "{\"tag\":\"p\",\"children\":[\"$it\"]}" }
        val content = "[" + nodes.joinToString(",") + "]"

        val response = httpClient.post("https://api.telegra.ph/createPage") {
            contentType(ContentType.Application.Json)
            parameter("access_token", account.accessToken)
            parameter("title", title)
            parameter("author_name", account.authorName)
            parameter("author_url", account.authorUrl)
            parameter("content", content)
            parameter("return_content", false)
        }
        println(response.request.url)

        println(response)

        return null
    }
}
