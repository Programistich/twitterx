package twitter.app.features.video

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
import twitterx.video.api.VideoService

@Component
public class VideoMessageExecutor(
    private val videoService: VideoService,
    private val localizationService: LocalizationService,
    private val telegramChatRepository: TelegramChatRepository,
    private val telegramClient: TelegramClient
) : Executor<TelegramMessageUpdate> {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override val priority: Executor.Priority = Executor.Priority.MEDIUM

    override suspend fun canProcess(context: TelegramContext<TelegramMessageUpdate>): Boolean {
        val text = context.update.text.trim()
        return videoService.isSupported(text)
    }

    override suspend fun process(context: TelegramContext<TelegramMessageUpdate>) {
        val text = context.update.text.trim()
        val chatId = context.update.chatId
        val messageId = context.update.messageId
        val userLanguage = getUserLanguage(context)

        logger.info("Processing video URL: $text for chat $chatId")

        // Send typing action
        telegramClient.sendChatAction(chatId, TelegramAction.TYPING)

        try {
            // Send video uploading action
            telegramClient.sendChatAction(chatId, TelegramAction.UPLOAD_VIDEO)

            // Download video
            val downloadResult = videoService.downloadVideo(text)

            if (downloadResult.isFailure) {
                handleVideoError(chatId, messageId, userLanguage, downloadResult.exceptionOrNull())
                return
            }

            val videoDownloadResult = downloadResult.getOrThrow()
            val videoFile = videoDownloadResult.file

            // Check if file exists and is readable
            if (!videoFile.exists() || !videoFile.canRead()) {
                logger.error("Video file does not exist or is not readable: ${videoFile.absolutePath}")
                handleVideoError(
                    chatId,
                    messageId,
                    userLanguage,
                    Exception("Video file not found or not readable")
                )
                return
            }

            // Send video file
            val videoResult = telegramClient.sendVideo(
                chatId,
                videoFile,
                "",
                "HTML",
                replyToMessageId = messageId,
            )

            if (videoResult.isFailure) {
                logger.error("Failed to send video to chat $chatId", videoResult.exceptionOrNull())
                handleVideoError(chatId, messageId, userLanguage, videoResult.exceptionOrNull())
            } else {
                logger.info("Successfully sent video to chat $chatId")
            }
        } catch (e: Exception) {
            logger.error("Unexpected error processing video", e)
            handleVideoError(chatId, messageId, userLanguage, e)
        }
    }

    private suspend fun handleVideoError(
        chatId: Long,
        messageId: Long,
        userLanguage: Language,
        error: Throwable?
    ) {
        logger.error("Failed to process video", error)

        val errorMessageKey = when (error) {
            is twitterx.video.api.VideoException.VideoFileTooLargeException -> MessageKey.VIDEO_FILE_TOO_LARGE
            is twitterx.video.api.VideoException.VideoProcessingTimeoutException -> MessageKey.VIDEO_PROCESSING_TIMEOUT
            is twitterx.video.api.VideoException.UnsupportedVideoUrlException -> MessageKey.VIDEO_UNSUPPORTED_URL
            else -> MessageKey.VIDEO_DOWNLOAD_ERROR
        }

        val message = localizationService.getMessage(errorMessageKey, userLanguage)

        val result = telegramClient.sendMessage(
            chatId,
            message,
            "HTML",
            replyToMessageId = messageId,
            disableWebPagePreview = true
        )

        if (result.isFailure) {
            logger.error("Failed to send video error message to chat $chatId", result.exceptionOrNull())
        } else {
            logger.info("Successfully sent video error message to chat $chatId")
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
}
