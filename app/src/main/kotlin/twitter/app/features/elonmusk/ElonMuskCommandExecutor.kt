package twitter.app.features.elonmusk

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
public class ElonMuskCommandExecutor(
    private val localizationService: LocalizationService,
    private val telegramChatRepository: TelegramChatRepository,
    private val telegramClient: TelegramClient
) : CommandExecutor() {
    public override val command: TelegramCommand = TelegramCommand.ELONMUSK

    override suspend fun process(context: TelegramContext<TelegramMessageUpdate>) {
        val chatId = context.update.chatId
        val userLanguage = getUserLanguage(context)

        // Get or create chat
        val chat = telegramChatRepository.findById(chatId)
            .orElseGet {
                val newChat = TelegramChat(chatId, userLanguage, false)
                telegramChatRepository.save(newChat)
            }

        // Toggle subscription state
        val wasSubscribed = chat.isElonMusk
        chat.isElonMusk = !wasSubscribed
        telegramChatRepository.save(chat)

        // Send appropriate message
        val messageKey = if (chat.isElonMusk) MessageKey.ELONMUSK_SUBSCRIBED else MessageKey.ELONMUSK_UNSUBSCRIBED
        val message = localizationService.getMessage(messageKey, userLanguage)

        val result = telegramClient.sendMessage(chatId, message, "HTML", disableWebPagePreview = true)
        if (result.isFailure) {
            logger.error("Failed to send elonmusk message to chat $chatId", result.exceptionOrNull())
        } else {
            logger.info(
                "Successfully sent elonmusk message to chat $chatId: ${if (chat.isElonMusk) "subscribed" else "unsubscribed"}"
            )
        }
    }

    private suspend fun getUserLanguage(context: TelegramContext<TelegramMessageUpdate>): Language {
        val chatId = context.update.chatId
        return telegramChatRepository.findById(chatId)
            .map { it.language }
            .orElse(Language.ENGLISH)
    }

    private companion object {
        private val logger = LoggerFactory.getLogger(ElonMuskCommandExecutor::class.java)
    }
}
