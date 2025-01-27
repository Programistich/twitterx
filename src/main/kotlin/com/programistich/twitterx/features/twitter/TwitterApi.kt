package com.programistich.twitterx.features.twitter

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.springframework.stereotype.Component

@Component
class TwitterApi(
    private val httpClient: HttpClient
) {
    suspend fun getTweet(id: String, lang: String): Result<Tweet> {
        return getTweetInternal(tweetId = id, lang = lang).map(::convert)
    }

    private fun convert(apiTweet: APITweet): Tweet {
        val apiAuthor = apiTweet.author

        val author = Author(username = apiAuthor.screenName, name = apiAuthor.name)
        return Tweet(
            id = apiTweet.id,
            text = apiTweet.text,
            author = author,
            content = TweetContent.from(apiTweet),
            translation = apiTweet.translation?.text
        )
    }

    private suspend fun getTweetInternal(tweetId: String, lang: String): Result<APITweet> {
        val url = "https://api.fxtwitter.com/status/$tweetId/$lang"
        val response = httpClient.get(url).body<APIResponse>()

        return when (response.code) {
            200 -> Result.success(response.tweet!!)
            401 -> Result.failure(PrivateTwitterException())
            404 -> Result.failure(NotFoundTwitterException())
            500 -> Result.failure(ApiFailTwitterException())
            else -> Result.failure(Exception("Unknown error"))
        }
    }
}
