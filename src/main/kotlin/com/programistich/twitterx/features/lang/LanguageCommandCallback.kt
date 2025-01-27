package com.programistich.twitterx.features.lang

import com.programistich.twitterx.core.telegram.models.Language
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class LanguageCommandCallback(
    @SerialName("chat_language")
    val newLanguage: Language,
    @SerialName("message_id")
    val commandMessageId: Int
) {
    fun encode(): String = Json.encodeToString(serializer = serializer(), value = this)
}
