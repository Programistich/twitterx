package twitterx.ai.api

import kotlinx.serialization.Serializable

/**
 * Enumeration of available AI models based on Gemini Web API
 */
public enum class AIModel(public val modelName: String) {
    UNSPECIFIED("unspecified"),
    GEMINI_2_5_FLASH("gemini-2.5-flash"),
    GEMINI_2_5_PRO("gemini-2.5-pro"),
    GEMINI_2_0_FLASH("gemini-2.0-flash"),
    GEMINI_2_0_FLASH_THINKING("gemini-2.0-flash-thinking");

    public companion object {
        public fun fromName(name: String): AIModel {
            return entries.find { it.modelName == name }
                ?: throw IllegalArgumentException("Unknown model name: $name")
        }
    }
}

/**
 * Request for AI text generation
 */
@Serializable
public data class AIRequest(
    public val prompt: String,
    public val model: AIModel = AIModel.UNSPECIFIED,
    public val files: List<String> = emptyList(),
    public val conversationId: String? = null
)

/**
 * Response from AI text generation
 */
@Serializable
public data class AIResponse(
    public val text: String,
    public val thoughts: String? = null,
    public val conversationId: String? = null,
    public val model: AIModel,
    public val images: List<AIImage> = emptyList()
)

/**
 * AI generated or retrieved image
 */
@Serializable
public data class AIImage(
    public val url: String,
    public val title: String = "[Image]",
    public val alt: String = "",
    public val isGenerated: Boolean = false
)

/**
 * Chat session metadata for multi-turn conversations
 */
@Serializable
public data class ChatSession(
    public val conversationId: String,
    public val model: AIModel = AIModel.UNSPECIFIED,
    public val metadata: List<String>? = null
)
