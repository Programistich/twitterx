// package twitter.app.features.ai
//
// import org.slf4j.LoggerFactory
// import org.springframework.stereotype.Component
// import twitter.app.repo.TelegramChat
// import twitter.app.repo.TelegramChatRepository
// import twitterx.ai.api.AIModel
// import twitterx.ai.api.AIService
// import twitterx.ai.api.ChatSession
// import twitterx.localization.api.LocalizationService
// import twitterx.localization.api.MessageKey
// import twitterx.telegram.api.TelegramClient
// import twitterx.telegram.api.executors.CommandExecutor
// import twitterx.telegram.api.models.TelegramCommand
// import twitterx.telegram.api.models.TelegramContext
// import twitterx.telegram.api.updates.TelegramMessageUpdate
// import twitterx.translation.api.Language
// import java.util.concurrent.ConcurrentHashMap
//
// @Component
// public class AICommandExecutor(
//    private val aiService: AIService,
//    private val localizationService: LocalizationService,
//    private val telegramClient: TelegramClient,
//    private val telegramChatRepository: TelegramChatRepository
// ) : CommandExecutor() {
//
//    override val command: TelegramCommand = TelegramCommand.AI
//
//    private val chatSessions = ConcurrentHashMap<Long, ChatSession>()
//
//    override suspend fun process(context: TelegramContext<TelegramMessageUpdate>) {
//        val chatId = context.update.chatId
//        val userLanguage = getUserLanguage(context)
//        val messageText = context.update.text
//
//        val prompt = extractPrompt(messageText)
//        if (prompt.isBlank()) {
//            val errorMessage = localizationService.getMessage(MessageKey.AI_ERROR, userLanguage)
//            sendMessage(chatId, errorMessage)
//            return
//        }
//
//        try {
//            // Check if AI service is available
//            if (!aiService.isAvailable()) {
//                val unavailableMessage = localizationService.getMessage(MessageKey.AI_UNAVAILABLE, userLanguage)
//                sendMessage(chatId, unavailableMessage)
//                return
//            }
//
//            // Get or create chat session
//            val session = getOrCreateSession(chatId)
//
//            // Send message to AI
//            val result = aiService.sendMessage(session, prompt)
//
//            result.fold(
//                onSuccess = { response ->
//                    val responsePrefix = localizationService.getMessage(MessageKey.AI_RESPONSE, userLanguage)
//                    val fullMessage = "$responsePrefix\n\n${response.text}"
//                    sendMessage(chatId, fullMessage)
//
//                    // Update session
//                    val updatedSession = session.copy(
//                        conversationId = response.conversationId ?: session.conversationId
//                    )
//                    chatSessions[chatId] = updatedSession
//                },
//                onFailure = { error ->
//                    logger.error("AI service failed for chat $chatId", error)
//                    val errorMessage = localizationService.getMessage(MessageKey.AI_ERROR, userLanguage)
//                    sendMessage(chatId, errorMessage)
//                }
//            )
//        } catch (e: Exception) {
//            logger.error("Unexpected error in AI command for chat $chatId", e)
//            val errorMessage = localizationService.getMessage(MessageKey.AI_ERROR, userLanguage)
//            sendMessage(chatId, errorMessage)
//        }
//    }
//
//    private fun extractPrompt(messageText: String): String {
//        // Remove "/ai " command prefix and return the rest as prompt
//        return messageText.substringAfter("/ai ").trim()
//    }
//
//    private suspend fun getOrCreateSession(chatId: Long): ChatSession {
//        return chatSessions[chatId] ?: run {
//            val result = aiService.startChat(AIModel.GEMINI_2_5_FLASH)
//            result.fold(
//                onSuccess = { session ->
//                    chatSessions[chatId] = session
//                    session
//                },
//                onFailure = { error ->
//                    logger.error("Failed to create AI session for chat $chatId", error)
//                    // Return a fallback session
//                    ChatSession(
//                        conversationId = "fallback_${chatId}_${System.currentTimeMillis()}",
//                        model = AIModel.GEMINI_2_5_FLASH,
//                        metadata = emptyList()
//                    )
//                }
//            )
//        }
//    }
//
//    private suspend fun getUserLanguage(context: TelegramContext<TelegramMessageUpdate>): Language {
//        val chatId = context.update.chatId
//        return telegramChatRepository.findById(chatId)
//            .map { it.language }
//            .orElseGet {
//                val newChat = TelegramChat(chatId, Language.ENGLISH)
//                telegramChatRepository.save(newChat)
//                Language.ENGLISH
//            }
//    }
//
//    private suspend fun sendMessage(chatId: Long, message: String) {
//        val result = telegramClient.sendMessage(chatId, message, "HTML")
//        if (result.isFailure) {
//            logger.error("Failed to send message to chat $chatId", result.exceptionOrNull())
//        }
//    }
//
//    private companion object {
//        private val logger = LoggerFactory.getLogger(AICommandExecutor::class.java)
//    }
// }
