package twitter.app.features.start

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import twitter.app.repo.TelegramChat
import twitter.app.repo.TelegramChatRepository
import twitterx.localization.api.LocalizationService
import twitterx.localization.api.MessageKey
import twitterx.telegram.api.TelegramClient
import twitterx.telegram.api.executors.CommandExecutor
import twitterx.telegram.api.models.TelegramCommand
import twitterx.telegram.api.models.TelegramContext
import twitterx.telegram.api.updates.TelegramMessageUpdate
import twitterx.translation.api.Language

@Component
public class StartCommandExecutor(
    private val localizationService: LocalizationService,
    private val telegramChatRepository: TelegramChatRepository,
    private val telegramClient: TelegramClient
) : CommandExecutor() {
    override val command: TelegramCommand = TelegramCommand.START

    override suspend fun process(context: TelegramContext<TelegramMessageUpdate>) {
        val chatId = context.update.chatId
        val userLanguage = getUserLanguage(context)

        val welcomeMessage = localizationService.getMessage(MessageKey.START_WELCOME, userLanguage)
        val instructionsMessage = localizationService.getMessage(MessageKey.START_INSTRUCTIONS, userLanguage)

        val fullMessage = "$welcomeMessage\n\n$instructionsMessage"

        // Send welcome message
        val result = telegramClient.sendMessage(chatId, fullMessage, "HTML", disableWebPagePreview = true)
        if (result.isFailure) {
            logger.error("Failed to send start message to chat $chatId", result.exceptionOrNull())
        } else {
            logger.info("Successfully sent start message to chat $chatId")
        }
    }

    private suspend fun getUserLanguage(context: TelegramContext<TelegramMessageUpdate>): Language {
        val chatId = context.update.chatId
        return telegramChatRepository.findById(chatId)
            .map { it.language }
            .orElseGet {
                // Create new chat with default language if not found
                val newChat = TelegramChat(chatId, Language.ENGLISH)
                telegramChatRepository.save(newChat)
                Language.ENGLISH
            }
    }

    private companion object {
        private val logger = LoggerFactory.getLogger(StartCommandExecutor::class.java)
    }
}
