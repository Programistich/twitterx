package twitterx.twitter.fx

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class FxTwitterResponse(
    val code: Int,
    val message: String,
    val tweet: FxTweet? = null
)

@Serializable
internal data class FxTweet(
    val id: String? = null,
    val url: String,
    val text: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("created_timestamp") val createdTimestamp: Long,
    val author: FxAuthor,
    val replies: Int = 0,
    val retweets: Int = 0,
    val likes: Int = 0,
    val views: Int? = null,
    val color: String? = null,
    @SerialName("twitter_card") val twitterCard: String? = null,
    val lang: String? = null,
    val source: String? = null,
    @SerialName("replying_to") val replyingTo: String? = null,
    @SerialName("replying_to_status") val replyingToStatus: String? = null,
    val media: FxMedia? = null,
    val quote: FxTweet? = null,
    val poll: FxPoll? = null,
    val translation: FxTranslation? = null
)

@Serializable
internal data class FxAuthor(
    val name: String,
    @SerialName("screen_name") val screenName: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("avatar_color") val avatarColor: String? = null,
    @SerialName("banner_url") val bannerUrl: String? = null
)

@Serializable
internal data class FxMedia(
    val all: List<FxMediaItem>? = null,
    val external: FxExternalMedia? = null,
    val photos: List<FxPhoto>? = null,
    val videos: List<FxVideo>? = null,
    val mosaic: FxMosaicPhoto? = null
)

@Serializable
internal data class FxMediaItem(
    val type: String,
    val url: String,
    val width: Int,
    val height: Int,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    val duration: Double? = null,
    val format: String? = null
)

@Serializable
internal data class FxPhoto(
    val type: String = "photo",
    val url: String,
    val width: Int,
    val height: Int
)

@Serializable
internal data class FxVideo(
    val type: String, // "video" or "gif"
    val url: String,
    @SerialName("thumbnail_url") val thumbnailUrl: String,
    val width: Int,
    val height: Int,
    val duration: Double? = null,
    val format: String
)

@Serializable
internal data class FxMosaicPhoto(
    val type: String = "mosaic_photo",
    val width: Int? = null,
    val height: Int? = null,
    val formats: FxMosaicFormats
)

@Serializable
internal data class FxMosaicFormats(
    val webp: String,
    val jpeg: String
)

@Serializable
internal data class FxExternalMedia(
    val type: String,
    val url: String,
    val height: Int,
    val width: Int,
    val duration: Double? = null
)

@Serializable
internal data class FxPoll(
    val choices: List<FxPollChoice>,
    @SerialName("total_votes") val totalVotes: Int,
    @SerialName("ends_at") val endsAt: String,
    @SerialName("time_left_en") val timeLeftEn: String
)

@Serializable
internal data class FxPollChoice(
    val label: String,
    val count: Int,
    val percentage: Double
)

@Serializable
internal data class FxTranslation(
    val text: String,
    @SerialName("source_lang") val sourceLang: String,
    @SerialName("target_lang") val targetLang: String
)
