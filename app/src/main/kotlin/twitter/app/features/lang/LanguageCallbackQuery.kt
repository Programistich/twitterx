package twitter.app.features.lang

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import twitter.app.repo.TelegramChat
import twitter.app.repo.TelegramChatRepository
import twitterx.localization.api.LocalizationService
import twitterx.localization.api.MessageKey
import twitterx.telegram.api.TelegramClient
import twitterx.telegram.api.executors.CallbackQueryExecutor
import twitterx.telegram.api.models.TelegramContext
import twitterx.telegram.api.updates.TelegramCallbackQueryUpdate
import twitterx.translation.api.Language

@Component
public class LanguageCallbackQuery(
    private val localizationService: LocalizationService,
    private val telegramChatRepository: TelegramChatRepository,
    private val telegramClient: TelegramClient
) : CallbackQueryExecutor<LanguageCommandCallback>() {
    override fun getDeserializer(): DeserializationStrategy<LanguageCommandCallback> {
        return LanguageCommandCallback.serializer()
    }

    override suspend fun process(context: TelegramContext<TelegramCallbackQueryUpdate>) {
        val chatId = context.update.chatId()

        try {
            // Parse callback data to get LanguageCommandCallback
            val callback = Json.decodeFromString(getDeserializer(), context.update.data)

            // Save or update user's language preference
            val existingChat = telegramChatRepository.findById(chatId)

            if (existingChat.isPresent) {
                // Update existing chat language
                val chat = existingChat.get()
                chat.language = callback.newLanguage
                telegramChatRepository.save(chat)
                logger.info("Updated language for chat $chatId to ${callback.newLanguage}")
            } else {
                // Create new chat with selected language
                val newChat = TelegramChat(chatId, callback.newLanguage)
                telegramChatRepository.save(newChat)
                logger.info("Created new chat $chatId with language ${callback.newLanguage}")
            }

            // Get confirmation message in the newly selected language
            val confirmationMessage = localizationService.getMessage(
                MessageKey.LANG_SELECTED,
                callback.newLanguage
            )

            // Send confirmation message
            val sendResult = telegramClient.sendMessage(
                chatId,
                confirmationMessage,
                "HTML",
                disableWebPagePreview = true
            )
            if (sendResult.isFailure) {
                logger.error("Failed to send language confirmation to chat $chatId", sendResult.exceptionOrNull())
            } else {
                logger.info("Successfully sent language confirmation to chat $chatId")
            }

            // Delete original command message
            val deleteResult = telegramClient.deleteMessage(chatId, callback.commandMessageId.toLong())
            if (deleteResult.isFailure) {
                logger.warn(
                    "Failed to delete original command message ${callback.commandMessageId} in chat $chatId",
                    deleteResult.exceptionOrNull()
                )
            }

            // Delete the callback query to remove it from the user's interface
            val deleteCallbackResult = telegramClient.deleteMessage(chatId, context.update.messageId)
            if (deleteCallbackResult.isFailure) {
                logger.warn(
                    "Failed to delete callback query message ${callback.commandMessageId} in chat $chatId",
                    deleteCallbackResult.exceptionOrNull()
                )
            }
        } catch (e: Exception) {
            logger.error("Failed to process language callback for chat $chatId", e)

            // Send error message in English as fallback
            val errorMessage = localizationService.getMessage(MessageKey.LANG_ERROR, Language.ENGLISH)
            val errorResult = telegramClient.sendMessage(chatId, errorMessage, "HTML", disableWebPagePreview = true)
            if (errorResult.isFailure) {
                logger.error("Failed to send error message to chat $chatId", errorResult.exceptionOrNull())
            }
        }
    }

    private companion object {
        private val logger = LoggerFactory.getLogger(LanguageCallbackQuery::class.java)
    }
}
