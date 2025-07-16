package twitterx.telegram.api.models.inline

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Base sealed interface for inline query results.
 */
@Serializable
public sealed interface InlineQueryResult {
    public val type: String
    public val id: String
}

/**
 * Inline query result for articles (text content).
 */
@Serializable
@SerialName("article")
public data class InlineQueryResultArticle(
    override val id: String,
    override val type: String = "article",
    val title: String,
    val description: String? = null,
    @SerialName("input_message_content")
    val inputMessageContent: InputMessageContent,
    @SerialName("thumb_url")
    val thumbUrl: String? = null,
    @SerialName("thumb_width")
    val thumbWidth: Int? = null,
    @SerialName("thumb_height")
    val thumbHeight: Int? = null
) : InlineQueryResult

/**
 * Inline query result for photos.
 */
@Serializable
@SerialName("photo")
public data class InlineQueryResultPhoto(
    override val id: String,
    override val type: String = "photo",
    @SerialName("photo_url")
    val photoUrl: String,
    @SerialName("thumb_url")
    val thumbUrl: String,
    @SerialName("photo_width")
    val photoWidth: Int? = null,
    @SerialName("photo_height")
    val photoHeight: Int? = null,
    val title: String? = null,
    val description: String? = null,
    val caption: String? = null,
    @SerialName("parse_mode")
    val parseMode: String? = null,
    @SerialName("input_message_content")
    val inputMessageContent: InputMessageContent? = null
) : InlineQueryResult
