package twitterx.telegram.api.models.inline

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Base sealed interface for input message content.
 */
@Serializable
public sealed interface InputMessageContent

/**
 * Text message content for inline results.
 */
@Serializable
public data class InputTextMessageContent(
    @SerialName("message_text")
    val messageText: String,
    @SerialName("parse_mode")
    val parseMode: String? = null,
    @SerialName("disable_web_page_preview")
    val disableWebPagePreview: Boolean? = null
) : InputMessageContent
