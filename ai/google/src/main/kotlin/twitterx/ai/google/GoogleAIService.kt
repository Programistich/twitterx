package twitterx.ai.google

import org.slf4j.LoggerFactory
import twitterx.ai.api.AIImage
import twitterx.ai.api.AIModel
import twitterx.ai.api.AIRequest
import twitterx.ai.api.AIResponse
import twitterx.ai.api.AIService
import twitterx.ai.api.ChatSession

/**
 * Google AI service implementation using Python Gemini Web API
 */
public class GoogleAIService(
    private val configuration: GoogleAIConfiguration
) : AIService {

    private val logger = LoggerFactory.getLogger(GoogleAIService::class.java)
    private val executor = PythonProcessExecutor(configuration)

    override suspend fun generateContent(request: AIRequest): Result<AIResponse> {
        logger.debug(
            "Generating content for prompt: {}, model: {}, files: {}",
            request.prompt.take(50),
            request.model.modelName,
            request.files.size
        )

        val command = PythonCommand(
            action = "generate",
            prompt = request.prompt,
            model = request.model.modelName,
            files = request.files,
            conversationId = request.conversationId,
            secure1PSID = configuration.secure1PSID,
            secure1PSIDTS = configuration.secure1PSIDTS,
            proxy = configuration.proxy,
            timeout = configuration.timeout
        )

        return executor.execute(command).mapCatching { pythonResponse ->
            logger.info(
                "Content generated successfully: model={}, responseLength={}, imagesCount={}",
                pythonResponse.model,
                pythonResponse.text?.length ?: 0,
                pythonResponse.images.size
            )

            AIResponse(
                text = pythonResponse.text ?: "",
                thoughts = pythonResponse.thoughts,
                conversationId = pythonResponse.conversationId,
                model = AIModel.fromName(pythonResponse.model ?: request.model.modelName),
                images = pythonResponse.images.map { image ->
                    AIImage(
                        url = image.url,
                        title = image.title,
                        alt = image.alt,
                        isGenerated = image.isGenerated
                    )
                }
            )
        }.onFailure { exception ->
            logger.error(
                "Content generation failed: model={}, promptLength={}",
                request.model.modelName,
                request.prompt.length,
                exception
            )
        }
    }

    override suspend fun startChat(model: AIModel): Result<ChatSession> {
        logger.debug("Starting chat session with model: {}", model.modelName)

        val command = PythonCommand(
            action = "start_chat",
            model = model.modelName,
            secure1PSID = configuration.secure1PSID,
            secure1PSIDTS = configuration.secure1PSIDTS,
            proxy = configuration.proxy,
            timeout = configuration.timeout
        )

        return executor.execute(command).mapCatching { pythonResponse ->
            val conversationId = pythonResponse.conversationId ?: generateConversationId()
            logger.info(
                "Chat session started successfully: conversationId={}, model={}",
                conversationId,
                model.modelName
            )

            ChatSession(
                conversationId = conversationId,
                model = model,
                metadata = pythonResponse.metadata
            )
        }.onFailure { exception ->
            logger.error("Failed to start chat session: model={}", model.modelName, exception)
        }
    }

    override suspend fun sendMessage(
        session: ChatSession,
        prompt: String,
        files: List<String>
    ): Result<AIResponse> {
        logger.debug(
            "Sending message in chat session: {}, promptLength={}, filesCount={}",
            session.conversationId,
            prompt.length,
            files.size
        )

        val command = PythonCommand(
            action = "send_message",
            prompt = prompt,
            model = session.model.modelName,
            files = files,
            conversationId = session.conversationId,
            metadata = session.metadata,
            secure1PSID = configuration.secure1PSID,
            secure1PSIDTS = configuration.secure1PSIDTS,
            proxy = configuration.proxy,
            timeout = configuration.timeout
        )

        return executor.execute(command).mapCatching { pythonResponse ->
            logger.info(
                "Message sent successfully in chat: conversationId={}, responseLength={}, imagesCount={}",
                session.conversationId,
                pythonResponse.text?.length ?: 0,
                pythonResponse.images.size
            )

            AIResponse(
                text = pythonResponse.text ?: "",
                thoughts = pythonResponse.thoughts,
                conversationId = pythonResponse.conversationId ?: session.conversationId,
                model = session.model,
                images = pythonResponse.images.map { image ->
                    AIImage(
                        url = image.url,
                        title = image.title,
                        alt = image.alt,
                        isGenerated = image.isGenerated
                    )
                }
            )
        }.onFailure { exception ->
            logger.error(
                "Failed to send message in chat: conversationId={}, promptLength={}",
                session.conversationId,
                prompt.length,
                exception
            )
        }
    }

    override suspend fun isAvailable(): Boolean {
        return try {
            val command = PythonCommand(
                action = "check_availability",
                secure1PSID = configuration.secure1PSID,
                secure1PSIDTS = configuration.secure1PSIDTS,
                proxy = configuration.proxy,
                timeout = configuration.timeout
            )

            executor.execute(command).isSuccess
        } catch (e: Exception) {
            logger.warn("AI service availability check failed", e)
            false
        }
    }

    override suspend fun close() {
        logger.debug("Closing Google AI service")
        // No resources to close for subprocess implementation
    }

    private fun generateConversationId(): String {
        return "chat_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}
