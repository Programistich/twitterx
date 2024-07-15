package com.programistich.twitterx.twitter.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Component

@Component
class TwitterApi {
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                }
            )
        }
    }

    suspend fun getTweet(tweetId: String): Result<APITweet> {
        val url = "https://api.fxtwitter.com/status/$tweetId"
        val response = httpClient.get(url).body<APIResponse>()

        return when (response.code) {
            200 -> Result.success(response.tweet!!)
            401 -> Result.failure(PrivateTweetException())
            404 -> Result.failure(NotFoundTweetException())
            500 -> Result.failure(ApiFailTweetException())
            else -> Result.failure(Exception("Unknown error"))
        }
    }
}

