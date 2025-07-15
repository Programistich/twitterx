package twitter.app.features.lang

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
import twitterx.telegram.api.models.keyboard.InlineKeyboardButton
import twitterx.telegram.api.models.keyboard.InlineKeyboardMarkup
import twitterx.telegram.api.updates.TelegramMessageUpdate
import twitterx.translation.api.Language

@Component
public class LanguageCommandExecutor(
    private val localizationService: LocalizationService,
    private val telegramChatRepository: TelegramChatRepository,
    private val telegramClient: TelegramClient
) : CommandExecutor() {
    override val command: TelegramCommand = TelegramCommand.LANG

    override suspend fun process(context: TelegramContext<TelegramMessageUpdate>) {
        val chatId = context.update.chatId
        val messageId = context.update.messageId
        val userLanguage = getUserLanguage(context)

        val title = localizationService.getMessage(MessageKey.LANG_TITLE, userLanguage)

        val languageOptions = buildString {
            for (language in Language.entries) {
                val languageName = when (language) {
                    Language.ENGLISH -> localizationService.getMessage(MessageKey.LANG_ENGLISH, language)
                    Language.UKRAINIAN -> localizationService.getMessage(MessageKey.LANG_UKRAINIAN, language)
                    Language.RUSSIAN -> localizationService.getMessage(MessageKey.LANG_RUSSIAN, language)
                }
                appendLine(languageName)
            }
        }.trimEnd()

        val fullMessage = "$title\n\n$languageOptions"

        // Create inline keyboard with language selection buttons
        val keyboard = createLanguageKeyboard(messageId)

        // Send message with inline keyboard
        val result = telegramClient.sendMessage(chatId, fullMessage, "HTML", keyboard, disableWebPagePreview = true)
        if (result.isFailure) {
            logger.error("Failed to send language selection to chat $chatId", result.exceptionOrNull())
        } else {
            logger.info("Successfully sent language selection to chat $chatId")
        }
    }

    private fun getLanguageEmoji(language: Language): String {
        return when (language) {
            Language.ENGLISH -> "🇬🇧"
            Language.UKRAINIAN -> "🇺🇦"
            Language.RUSSIAN -> "🇷🇺"
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

    private fun createLanguageKeyboard(messageId: Long): InlineKeyboardMarkup {
        val buttons = Language.entries.map { language ->
            val callback = LanguageCommandCallback(language, messageId.toInt())
            val emoji = getLanguageEmoji(language)
            val text = "$emoji ${language.name.lowercase().replaceFirstChar { it.uppercase() }}"

            listOf(InlineKeyboardButton(text, callbackData = callback.encode()))
        }

        return InlineKeyboardMarkup(buttons)
    }

    private companion object {
        private val logger = LoggerFactory.getLogger(LanguageCommandExecutor::class.java)
    }
}
