package twitter.app.features.elonmusk

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import twitter.app.features.twitter.ElonMuskCommandExecutor
import twitter.app.repo.TelegramChat
import twitter.app.repo.TelegramChatRepository
import twitterx.localization.api.LocalizationService
import twitterx.localization.api.MessageKey
import twitterx.telegram.api.TelegramClient
import twitterx.telegram.api.models.TelegramCommand
import twitterx.telegram.api.models.TelegramConfig
import twitterx.telegram.api.models.TelegramContext
import twitterx.telegram.api.models.response.TelegramMessage
import twitterx.telegram.api.updates.TelegramMessageUpdate
import twitterx.translation.api.Language
import java.util.Optional
import kotlin.test.assertTrue
import twitterx.telegram.api.models.response.TelegramChat as TelegramChatResponse

class ElonMuskCommandExecutorTest {

    private val localizationService = mockk<LocalizationService>()
    private val telegramChatRepository = mockk<TelegramChatRepository>()
    private val telegramClient = mockk<TelegramClient>()

    private lateinit var executor: ElonMuskCommandExecutor

    @BeforeEach
    fun setUp() {
        executor = ElonMuskCommandExecutor(
            localizationService = localizationService,
            telegramChatRepository = telegramChatRepository,
            telegramClient = telegramClient
        )
    }

    @Test
    fun `should subscribe new user to Elon Musk tweets`() = runTest {
        // Given
        val chatId = 123L
        val language = Language.ENGLISH
        val update = TelegramMessageUpdate(
            text = "/elonmusk",
            messageId = 1L,
            chatId = chatId,
            command = TelegramCommand.ELONMUSK,
            name = "test-user",
            updateId = 1L
        )
        val config = TelegramConfig("test-token", "test-bot", "123")
        val context = TelegramContext(update, config)
        val successMessage = "✅ You are now subscribed to Elon Musk's tweets!"
        val telegramMessage = TelegramMessage(
            messageId = 1L,
            chat = TelegramChatResponse(chatId, "private")
        )

        coEvery { telegramChatRepository.findById(chatId) } returns Optional.empty()
        coEvery { localizationService.getMessage(MessageKey.ELONMUSK_SUBSCRIBED, language) } returns successMessage
        coEvery {
            telegramClient.sendMessage(chatId, successMessage, "HTML", disableWebPagePreview = true)
        } returns Result.success(telegramMessage)
        coEvery { telegramChatRepository.save(any()) } answers { arg<TelegramChat>(0) }

        // When
        executor.process(context)

        // Then
        coVerify { telegramChatRepository.save(any()) }
        coVerify { telegramClient.sendMessage(chatId, successMessage, "HTML", disableWebPagePreview = true) }
    }

    @Test
    fun `should toggle subscription for existing user`() = runTest {
        // Given
        val chatId = 123L
        val language = Language.ENGLISH
        val existingChat = TelegramChat(chatId, language, false)
        val update = TelegramMessageUpdate(
            text = "/elonmusk",
            messageId = 1L,
            chatId = chatId,
            command = TelegramCommand.ELONMUSK,
            name = "test-user",
            updateId = 1L
        )
        val config = TelegramConfig("test-token", "test-bot", "123")
        val context = TelegramContext(update, config)
        val successMessage = "✅ You are now subscribed to Elon Musk's tweets!"
        val telegramMessage = TelegramMessage(
            messageId = 1L,
            chat = TelegramChatResponse(chatId, "private")
        )

        coEvery { telegramChatRepository.findById(chatId) } returns Optional.of(existingChat)
        coEvery { localizationService.getMessage(MessageKey.ELONMUSK_SUBSCRIBED, language) } returns successMessage
        coEvery {
            telegramClient.sendMessage(chatId, successMessage, "HTML", disableWebPagePreview = true)
        } returns Result.success(telegramMessage)
        coEvery { telegramChatRepository.save(any()) } answers { arg<TelegramChat>(0) }

        // When
        executor.process(context)

        // Then
        coVerify { telegramChatRepository.save(match { it.isElonMusk == true }) }
        coVerify { telegramClient.sendMessage(chatId, successMessage, "HTML", disableWebPagePreview = true) }
    }

    @Test
    fun `should unsubscribe user from Elon Musk tweets`() = runTest {
        // Given
        val chatId = 123L
        val language = Language.ENGLISH
        val existingChat = TelegramChat(chatId, language, true)
        val update = TelegramMessageUpdate(
            text = "/elonmusk",
            messageId = 1L,
            chatId = chatId,
            command = TelegramCommand.ELONMUSK,
            name = "test-user",
            updateId = 1L
        )
        val config = TelegramConfig("test-token", "test-bot", "123")
        val context = TelegramContext(update, config)
        val unsubscribeMessage = "❌ You have unsubscribed from Elon Musk's tweets."
        val telegramMessage = TelegramMessage(
            messageId = 1L,
            chat = TelegramChatResponse(chatId, "private")
        )

        coEvery { telegramChatRepository.findById(chatId) } returns Optional.of(existingChat)
        coEvery { localizationService.getMessage(MessageKey.ELONMUSK_UNSUBSCRIBED, language) } returns unsubscribeMessage
        coEvery {
            telegramClient.sendMessage(chatId, unsubscribeMessage, "HTML", disableWebPagePreview = true)
        } returns Result.success(telegramMessage)
        coEvery { telegramChatRepository.save(any()) } answers { arg<TelegramChat>(0) }

        // When
        executor.process(context)

        // Then
        coVerify { telegramChatRepository.save(match { it.isElonMusk == false }) }
        coVerify { telegramClient.sendMessage(chatId, unsubscribeMessage, "HTML", disableWebPagePreview = true) }
    }

    @Test
    fun `should handle different languages`() = runTest {
        // Given
        val chatId = 123L
        val language = Language.UKRAINIAN
        val existingChat = TelegramChat(chatId, language, false)
        val update = TelegramMessageUpdate(
            text = "/elonmusk",
            messageId = 1L,
            chatId = chatId,
            command = TelegramCommand.ELONMUSK,
            name = "test-user",
            updateId = 1L
        )
        val config = TelegramConfig("test-token", "test-bot", "123")
        val context = TelegramContext(update, config)
        val successMessage = "✅ Ви підписалися на твіти Ілона Маска!"
        val telegramMessage = TelegramMessage(
            messageId = 1L,
            chat = TelegramChatResponse(chatId, "private")
        )

        coEvery { telegramChatRepository.findById(chatId) } returns Optional.of(existingChat)
        coEvery { localizationService.getMessage(MessageKey.ELONMUSK_SUBSCRIBED, language) } returns successMessage
        coEvery {
            telegramClient.sendMessage(chatId, successMessage, "HTML", disableWebPagePreview = true)
        } returns Result.success(telegramMessage)
        coEvery { telegramChatRepository.save(any()) } answers { arg<TelegramChat>(0) }

        // When
        executor.process(context)

        // Then
        coVerify { localizationService.getMessage(MessageKey.ELONMUSK_SUBSCRIBED, language) }
        coVerify { telegramClient.sendMessage(chatId, successMessage, "HTML", disableWebPagePreview = true) }
    }

    @Test
    fun `should handle telegram client failure`() = runTest {
        // Given
        val chatId = 123L
        val language = Language.ENGLISH
        val existingChat = TelegramChat(chatId, language, false)
        val update = TelegramMessageUpdate(
            text = "/elonmusk",
            messageId = 1L,
            chatId = chatId,
            command = TelegramCommand.ELONMUSK,
            name = "test-user",
            updateId = 1L
        )
        val config = TelegramConfig("test-token", "test-bot", "123")
        val context = TelegramContext(update, config)
        val successMessage = "✅ You are now subscribed to Elon Musk's tweets!"
        val exception = RuntimeException("Network error")

        coEvery { telegramChatRepository.findById(chatId) } returns Optional.of(existingChat)
        coEvery { localizationService.getMessage(MessageKey.ELONMUSK_SUBSCRIBED, language) } returns successMessage
        coEvery {
            telegramClient.sendMessage(chatId, successMessage, "HTML", disableWebPagePreview = true)
        } returns Result.failure(exception)
        coEvery { telegramChatRepository.save(any()) } answers { arg<TelegramChat>(0) }

        // When
        executor.process(context)

        // Then
        coVerify { telegramChatRepository.save(any()) }
        coVerify { telegramClient.sendMessage(chatId, successMessage, "HTML", disableWebPagePreview = true) }
    }

    @Test
    fun `should verify executor can process elonmusk command`() = runTest {
        // Given
        val chatId = 123L
        val update = TelegramMessageUpdate(
            text = "/elonmusk",
            messageId = 1L,
            chatId = chatId,
            command = TelegramCommand.ELONMUSK,
            name = "test-user",
            updateId = 1L
        )
        val config = TelegramConfig("test-token", "test-bot", "123")
        val context = TelegramContext(update, config)

        // When
        val canProcess = executor.canProcess(context)

        // Then
        assertTrue(canProcess)
    }
}
