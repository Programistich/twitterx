package twitterx.ai.api

/**
 * Main interface for AI text generation services
 */
public interface AIService {

    /**
     * Generate text content using AI model
     *
     * @param request Request containing prompt, model preferences, and optional files
     * @return Result containing AI response or error
     */
    public suspend fun generateContent(request: AIRequest): Result<AIResponse>

    /**
     * Start a new chat session for multi-turn conversations
     *
     * @param model Model to use for the chat session
     * @return Chat session metadata
     */
    public suspend fun startChat(model: AIModel = AIModel.UNSPECIFIED): Result<ChatSession>

    /**
     * Send message in existing chat session
     *
     * @param session Chat session to continue
     * @param prompt User message
     * @param files Optional files to attach
     * @return AI response with updated conversation context
     */
    public suspend fun sendMessage(
        session: ChatSession,
        prompt: String,
        files: List<String> = emptyList()
    ): Result<AIResponse>

    /**
     * Check if the service is available and properly configured
     *
     * @return True if service is ready, false otherwise
     */
    public suspend fun isAvailable(): Boolean

    /**
     * Close any resources used by the service
     */
    public suspend fun close()
}
