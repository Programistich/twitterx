package twitterx.telegram.api.models.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Telegram message response model.
 */
@Serializable
public data class TelegramMessage(
    @SerialName("message_id")
    val messageId: Long,
    val chat: TelegramChat,
    val text: String? = null,
    val caption: String? = null
)

/**
 * Telegram chat information.
 */
@Serializable
public data class TelegramChat(
    val id: Long,
    val type: String
)
