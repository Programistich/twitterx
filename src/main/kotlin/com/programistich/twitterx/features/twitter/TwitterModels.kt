package com.programistich.twitterx.features.twitter

data class Tweet(
    val id: String,
    val text: String,
    val author: Author,
    val content: TweetContent,
    val translation: String?,
) {
    val url by lazy { "https://x.com/${author.username}/status/$id" }
}

sealed class TweetContent {
    data object Text : TweetContent()
    data class Photo(val url: String) : TweetContent()
    data class Video(val url: String, val thumbnailUrl: String) : TweetContent()
    data class Poll(val options: List<String>) : TweetContent()
    data class ManyMedia(val urls: List<String>, val mosaic: String?) : TweetContent()

    companion object {
        fun from(apiTweet: APITweet): TweetContent {
            return when {
                apiTweet.poll != null -> Poll(apiTweet.poll.choices.map { it.label })
                apiTweet.media?.videos == null && apiTweet.media?.photos == null -> Text
                apiTweet.media.videos == null && apiTweet.media.photos?.size == 1 -> {
                    Photo(apiTweet.media.photos[0].url)
                }
                apiTweet.media.photos == null && apiTweet.media.videos?.size == 1 -> {
                    Video(apiTweet.media.videos[0].url, apiTweet.media.videos[0].thumbnailUrl)
                }
                else -> {
                    val photoUrls = apiTweet.media.photos?.map { it.url } ?: emptyList()
                    val videoUrls = apiTweet.media.videos?.map { it.url } ?: emptyList()
                    ManyMedia(photoUrls + videoUrls, apiTweet.media.mosaic?.formats?.jpeg)
                }
            }
        }
    }
}

data class Author(
    val username: String,
    val name: String
) {
    val url by lazy { "https://x.com/$username" }
}
