package twitter.app.features.twitter

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import twitter.app.repo.TelegramChat
import twitter.app.repo.TelegramChatRepository
import twitterx.localization.api.LocalizationService
import twitterx.localization.api.MessageKey
import twitterx.telegram.api.TelegramAction
import twitterx.telegram.api.TelegramClient
import twitterx.telegram.api.executors.Executor
import twitterx.telegram.api.models.TelegramContext
import twitterx.telegram.api.updates.TelegramMessageUpdate
import twitterx.translation.api.Language
import twitterx.twitter.api.TwitterService

@Component
public class TweetMessageExecutor(
    private val twitterService: TwitterService,
    private val telegramChatRepository: TelegramChatRepository,
    private val telegramClient: TelegramClient,
    private val tweetSender: TweetSender,
    private val localizationService: LocalizationService
) : Executor<TelegramMessageUpdate> {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override val priority: Executor.Priority = Executor.Priority.MEDIUM

    override suspend fun canProcess(context: TelegramContext<TelegramMessageUpdate>): Boolean {
        val text = context.update.text.trim()
        return twitterService.getTweetId(text).isSuccess
    }

    override suspend fun process(context: TelegramContext<TelegramMessageUpdate>) {
        val text = context.update.text.trim()
        val chatId = context.update.chatId
        val messageId = context.update.messageId
        val userLanguage = getUserLanguage(context)

        telegramClient.sendChatAction(chatId, TelegramAction.TYPING)

        val tweetIdResult = twitterService.getTweetId(text)
        if (tweetIdResult.isFailure) {
            logger.error("Failed to extract tweet ID from: $text")
            return
        }

        val tweetId = tweetIdResult.getOrThrow()

        // We need to extract username from the URL for getTweetThread
        val username = twitterService.getUsername(text).getOrThrow()

        val tweetThreadResult = twitterService.getTweetThread(username, tweetId)

        if (tweetThreadResult.isFailure) {
            handleTweetError(
                chatId = chatId,
                messageId = messageId,
                userLanguage = userLanguage,
                error = tweetThreadResult.exceptionOrNull()
            )
            return
        }

        val tweetThread = tweetThreadResult.getOrThrow()

        tweetSender.sendTweetThread(tweetThread, chatId, userLanguage, from = context.update.name)

        deleteTweetMessage(messageId = context.update.messageId, chatId = chatId)
    }

    private suspend fun handleTweetError(
        chatId: Long,
        messageId: Long,
        userLanguage: Language,
        error: Throwable?
    ) {
        logger.error("Failed to fetch tweet", error)
        val message = localizationService.getMessage(MessageKey.TWEET_ERROR, userLanguage)

        val result = telegramClient.sendMessage(
            chatId,
            message,
            "HTML",
            replyToMessageId = messageId,
            disableWebPagePreview = true
        )
        if (result.isFailure) {
            logger.error("Failed to send tweet error message to chat $chatId", result.exceptionOrNull())
        } else {
            logger.info("Successfully sent tweet error message to chat $chatId")
        }
    }

    private fun getUserLanguage(context: TelegramContext<TelegramMessageUpdate>): Language {
        val chatId = context.update.chatId
        return getUserLanguageFromChatId(chatId)
    }

    private fun getUserLanguageFromChatId(chatId: Long): Language {
        return telegramChatRepository.findById(chatId)
            .map { it.language }
            .orElseGet {
                // Create new chat with default language if not found
                val newChat = TelegramChat(chatId, Language.ENGLISH)
                telegramChatRepository.save(newChat)
                Language.ENGLISH
            }
    }

    private suspend fun deleteTweetMessage(messageId: Long, chatId: Long) {
        telegramClient.deleteMessage(chatId, messageId)
            .onFailure { logger.error("Failed to delete tweet message $messageId in chat $chatId", it) }
            .onSuccess { logger.info("Successfully deleted tweet message $messageId in chat $chatId") }
    }
}
