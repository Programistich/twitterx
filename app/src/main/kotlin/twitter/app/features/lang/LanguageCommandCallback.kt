package twitter.app.features.lang

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import twitterx.translation.api.Language

@Serializable
public data class LanguageCommandCallback(
    @SerialName("chat_language")
    val newLanguage: Language,
    @SerialName("message_id")
    val commandMessageId: Int
) {
    public fun encode(): String = Json.encodeToString(serializer = serializer(), value = this)
}
