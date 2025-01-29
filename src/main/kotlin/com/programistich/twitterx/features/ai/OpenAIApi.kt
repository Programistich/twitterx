package com.programistich.twitterx.features.ai

import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.core.Role
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.programistich.twitterx.core.telegram.models.Language
import org.springframework.stereotype.Component

@Component
class OpenAIApi(
    private val openAI: OpenAI
) {
    companion object {
        private const val SIZE_LIMITER = "Please limit your answer to four sentences, and refrain from using any Markdown or HTML."
    }

    suspend fun request(
        mainMessage: String,
        replyMessage: String?,
        language: Language
    ): ChatCompletion {
        val messages = mutableListOf<ChatMessage>()
        if (replyMessage != null) {
            messages.add(
                ChatMessage(
                    Role.User,
                    content = replyMessage
                )
            )
            messages.add(
                ChatMessage(
                    Role.System,
                    content = "The user is asking to provide a helpful reply to the above message."
                )
            )
        }

        messages.add(
            ChatMessage(
                Role.User,
                content = mainMessage
            )
        )

        messages.add(
            ChatMessage(
                Role.System,
                content = listOf(
                    "An online conversation took place, where a user asked a question and received the following response.",
                    "Your task is to provide a clear and concise reply in the language defined by the ISO code: ${language.iso}.",
                    SIZE_LIMITER
                ).joinToString("\n")
            )
        )

        val request = createRequest(messages)
        return openAI.chatCompletion(request)
    }

    private fun createRequest(
        chatMessages: List<ChatMessage>,
    ): ChatCompletionRequest {
        return ChatCompletionRequest(
            model = ModelId("gpt-4o-mini"),
            messages = chatMessages,
            temperature = 0.9,
            topP = 1.0,
            frequencyPenalty = 0.0,
            presencePenalty = 0.6
        )
    }
}
