package com.programistich.twitterx.twitter.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class APIResponse(
    @SerialName("code") val code: Int,
    @SerialName("message") val message: String,
    @SerialName("tweet") val tweet: APITweet? = null
)

@Serializable
data class APITweet(
    @SerialName("id") val id: String,
    @SerialName("url") val url: String,
    @SerialName("text") val text: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("created_timestamp") val createdTimestamp: Long,
    @SerialName("color") val color: String? = null,
    @SerialName("lang") val lang: String? = null,
    @SerialName("replying_to") val replyingTo: String? = null,
    @SerialName("replying_to_status") val replyingToStatus: String? = null,
    @SerialName("twitter_card") val twitterCard: String? = null,
    @SerialName("author") val author: APIAuthor,
    @SerialName("source") val source: String? = null,
    @SerialName("likes") val likes: Int,
    @SerialName("retweets") val retweets: Int,
    @SerialName("replies") val replies: Int,
    @SerialName("views") val views: Int? = null,
    @SerialName("quote") val quote: APITweet? = null,
    @SerialName("poll") val poll: APIPoll? = null,
    @SerialName("translation") val translation: APITranslate? = null,
    @SerialName("media") val media: Media? = null
)

@Serializable
data class APIAuthor(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("screen_name") val screenName: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("avatar_color") val avatarColor: String? = null,
    @SerialName("banner_url") val bannerUrl: String? = null
)

@Serializable
data class APITranslate(
    @SerialName("text") val text: String,
    @SerialName("source_lang") val sourceLang: String,
    @SerialName("target_lang") val targetLang: String
)

@Serializable
data class APIExternalMedia(
    @SerialName("type") val type: String,
    @SerialName("url") val url: String,
    @SerialName("height") val height: Int,
    @SerialName("width") val width: Int
)

@Serializable
data class APIPoll(
    @SerialName("choices") val choices: List<APIPollChoice>,
    @SerialName("total_votes") val totalVotes: Int,
    @SerialName("ends_at") val endsAt: String,
    @SerialName("time_left_en") val timeLeftEn: String
)

@Serializable
data class APIPollChoice(
    @SerialName("label") val label: String,
    @SerialName("count") val count: Int,
    @SerialName("percentage") val percentage: Double
)

@Serializable
data class APIPhoto(
    @SerialName("type") val type: String,
    @SerialName("url") val url: String,
    @SerialName("width") val width: Int,
    @SerialName("height") val height: Int,
    @SerialName("altText") val altText: String? = null
)

@Serializable
data class APIMosaicPhoto(
    @SerialName("type") val type: String,
    @SerialName("width") val width: Int? = null,
    @SerialName("height") val height: Int? = null,
    @SerialName("formats") val formats: Formats
)

@Serializable
data class Formats(
    @SerialName("webp") val webp: String,
    @SerialName("jpeg") val jpeg: String
)

@Serializable
data class APIVideo(
    @SerialName("type") val type: String,
    @SerialName("url") val url: String,
    @SerialName("thumbnail_url") val thumbnailUrl: String,
    @SerialName("width") val width: Int,
    @SerialName("height") val height: Int,
    @SerialName("format") val format: String
)

@Serializable
data class Media(
    @SerialName("external") val external: APIExternalMedia? = null,
    @SerialName("photos") val photos: List<APIPhoto>? = null,
    @SerialName("videos") val videos: List<APIVideo>? = null,
    @SerialName("mosaic") val mosaic: APIMosaicPhoto? = null
)
