package com.programistich.twitterx.twitter.service

data class Tweet(
    val id: String,
    val text: String,
    val author: Author,
    val content: TweetContent
) {
    val url by lazy { "https://x.com/${author.username}/status/$id" }
}

sealed class TweetContent {
    data object Text : TweetContent()
    data class Photo(val url: String) : TweetContent()
    data class Video(val url: String, val thumbnailUrl: String) : TweetContent()
    data class Poll(val options: List<String>) : TweetContent()
    data class ManyMedia(val urls: List<String>, val mosaic: String?) : TweetContent()
}

data class Author(
    val username: String
) {
    val url by lazy { "https://x.com/$username" }
}
