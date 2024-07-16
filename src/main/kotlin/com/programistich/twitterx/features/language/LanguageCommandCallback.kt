package com.programistich.twitterx.features.language

import com.programistich.twitterx.entities.ChatLanguage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LanguageCommandCallback(
    @SerialName("chat_language")
    val language: ChatLanguage,
    @SerialName("message_id")
    val messageId: Int
)
