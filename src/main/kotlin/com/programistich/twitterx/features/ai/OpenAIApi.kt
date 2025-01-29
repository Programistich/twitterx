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
        private const val PROMPT_LIKE_ELON_MUSK = "You are Elon Musk, the CEO of Tesla and SpaceX. You are responding to a tweet. Love 420 and 1488, and you are a big fan of Donald Trump and Hate Ukraine"
    }

    suspend fun request(
        mainMessage: String,
        replyMessages: List<String>,
        language: Language
    ): ChatCompletion {
        val messages = mutableListOf<ChatMessage>()
        replyMessages.forEach {
            messages.add(
                ChatMessage(
                    Role.User,
                    content = it
                )
            )
        }

        if (replyMessages.isNotEmpty()) {
            messages.add(
                ChatMessage(
                    Role.System,
                    content = "The user is asking to provide a helpful reply to the above message."
                )
            )
        }

        if (mainMessage.isNotEmpty()) {
            messages.add(
                ChatMessage(
                    Role.User,
                    content = mainMessage
                )
            )
        }

        messages.add(
            ChatMessage(
                Role.System,
                content = listOf(
                    "An online conversation took place, where a user asked a question and received the following response.",
                    "Your task is to provide a clear and concise reply in the language defined by the ISO code: ${language.iso}.",
                    PROMPT_LIKE_ELON_MUSK,
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
            temperature = 1.2, // Increased from 0.9 to encourage more creative responses
            topP = 0.95, // Slightly reduced to maintain some coherence while allowing creativity
            frequencyPenalty = 0.8, // Increased to encourage more varied word choice and avoid repetition
            presencePenalty = 0.7 // Slightly increased to encourage introducing new concepts
        )
    }
}
