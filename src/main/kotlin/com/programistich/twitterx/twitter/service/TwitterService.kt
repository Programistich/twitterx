package com.programistich.twitterx.twitter.service

import com.programistich.twitterx.twitter.api.APITweet
import com.programistich.twitterx.twitter.api.TwitterApi
import org.springframework.stereotype.Component

@Component
class TwitterService(private val twitterApi: TwitterApi) {
    suspend fun getTweet(id: String): Result<Tweet> {
        return twitterApi.getTweet(id).map(::convert)
    }

    private fun convert(apiTweet: APITweet): Tweet {
        val apiAuthor = apiTweet.author

        val author = Author(username = apiAuthor.screenName)
        return Tweet(
            id = apiTweet.id,
            text = apiTweet.text,
            author = author,
            content = convertToContent(apiTweet)
        )
    }

    private fun convertToContent(apiTweet: APITweet): TweetContent {
        return when {
            apiTweet.poll != null -> TweetContent.Poll(apiTweet.poll.choices.map { it.label })
            apiTweet.media?.videos == null && apiTweet.media?.photos == null -> TweetContent.Text
            apiTweet.media.videos == null && apiTweet.media.photos?.size == 1 -> {
                TweetContent.Photo(apiTweet.media.photos[0].url)
            }
            apiTweet.media.photos == null && apiTweet.media.videos?.size == 1 -> {
                TweetContent.Video(apiTweet.media.videos[0].url)
            }
            else -> {
                val photoUrls = apiTweet.media.photos?.map { it.url } ?: emptyList()
                val videoUrls = apiTweet.media.videos?.map { it.url } ?: emptyList()
                TweetContent.ManyMedia(photoUrls + videoUrls)
            }
        }
    }
}
