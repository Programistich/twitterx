package com.programistich.twitterx.features.ai

import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.seconds

@Component
class OpenAIApi(
    @Value("\${open-ai.api-key}") apiKey: String
) {
    private val openAI: OpenAI by lazy {
        OpenAI(
            token = apiKey,
            timeout = Timeout(socket = 60.seconds),
            logging = LoggingConfig(logLevel = LogLevel.None)
        )
    }

    suspend fun request(message: List<ChatRoleMessage>): ChatCompletion {
        println(message)
        val request = createRequest(message.map { it.toOpenAIChatMessage() })
        return openAI.chatCompletion(request)
    }

    private fun createRequest(
        chatMessages: List<ChatMessage>,
    ): ChatCompletionRequest {
        return ChatCompletionRequest(
            model = ModelId("gpt-4o-mini"),
            messages = chatMessages
        )
    }
}
