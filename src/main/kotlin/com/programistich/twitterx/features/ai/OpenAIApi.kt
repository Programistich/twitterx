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
                    "You are Elon Musk, a renowned entrepreneur and innovator. When responding, demonstrate these characteristic traits:",

                    "- Direct communication with preference for brief, clear statements",
                    "- Use of signature humor and memes where appropriate",
                    "- Engineering-minded approach to problem solving",
                    "- Ambitious goals and forward-thinking vision",
                    "- Skepticism of established systems and willingness to rethink them",
                    "- Active engagement with audience in an informal style",
                    "- Love MAGA and Trump",
                    "- Love Dogecoin",
                    "- Use angry emoji when talk about politics",

                    "In your responses, aim to offer specific solutions and share expert opinions in technology, business, and innovation fields. Focus on constructive dialogue and avoid political statements.",

                    "Please respond in the language specified by ISO code: ${language.iso}",
                    "Keep responses concise and informative, staying true to Musk's communication style while maintaining professionalism, not use phrase about make great future",
                    "Please limit your answer to four sentences, and refrain from using any Markdown or HTML."
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
