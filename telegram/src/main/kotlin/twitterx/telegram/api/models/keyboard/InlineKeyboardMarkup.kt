package twitterx.telegram.api.models.keyboard

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Inline keyboard markup for Telegram messages.
 */
@Serializable
public data class InlineKeyboardMarkup(
    @SerialName("inline_keyboard")
    val inlineKeyboard: List<List<InlineKeyboardButton>>
)

/**
 * Individual inline keyboard button.
 */
@Serializable
public data class InlineKeyboardButton(
    val text: String,
    @SerialName("callback_data")
    val callbackData: String? = null,
    val url: String? = null
)
