package twitterx.twitter.api

import java.time.LocalDateTime

public data class Tweet(
    val id: String,
    val username: String,
    val fullName: String,
    val content: String,
    val createdAt: LocalDateTime,
    val mediaUrls: List<String>,
    val videoUrls: List<String>,
    val replyToTweetId: String?,
    val retweetOfTweetId: String?,
    val quoteTweetId: String?,
    val profileImageUrl: String?,
    val language: String?,
    val hashtags: List<String>,
    val mentions: List<String>,
    val urls: List<String>
) {
    public val tweetUrl: String get() = "https://x.com/$username/status/$id"

    public val isReply: Boolean get() = replyToTweetId != null

    public val isQuote: Boolean get() = quoteTweetId != null

    public val isRetweet: Boolean get() = retweetOfTweetId != null

    public val account: TwitterAccount get() = TwitterAccount(username, fullName)
}

public data class TwitterAccount(
    val username: String,
    val name: String
) {
    val url: String = "https://x.com/$username"
}
