package twitterx.translation.openai

import kotlinx.serialization.Serializable

// Request models for OpenRouter /responses API

@Serializable
internal data class ResponsesRequest(
    val model: String,
    val input: String,
    val text: ResponsesText? = null
)

@Serializable
internal data class ResponsesText(
    val format: ResponseFormat
)

@Serializable
internal data class ResponseFormat(
    val type: String
)

// Response models for OpenRouter /responses API (partial, only fields we use)

@Serializable
internal data class ResponsesResponse(
    val output: List<ResponseOutputItem> = emptyList()
)

@Serializable
internal data class ResponseOutputItem(
    val id: String? = null,
    val type: String,
    val status: String? = null,
    val role: String? = null,
    val content: List<ResponseContent>? = null
)

@Serializable
internal data class ResponseContent(
    val type: String,
    val text: String? = null
)

@Serializable
internal data class TranslationResult(
    val text: String,
    val sourceLanguage: String
)
