package com.programistich.twitterx.features.ai

import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.core.Role

sealed class ChatRoleMessage {
    data class User(val content: String) : ChatRoleMessage()
    data class System(val content: String) : ChatRoleMessage()
    data class Assistant(val content: String) : ChatRoleMessage()

    fun toOpenAIChatMessage(): ChatMessage {
        return when (this) {
            is User -> ChatMessage(Role.User, content)
            is System -> ChatMessage(Role.System, content)
            is Assistant -> ChatMessage(Role.Assistant, content)
        }
    }
}
