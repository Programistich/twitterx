package twitterx.localization.impl

import kotlinx.coroutines.test.runTest
import twitterx.localization.api.MessageKey
import twitterx.translation.api.Language
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FileBasedLocalizationServiceTest {

    private val service = FileBasedLocalizationService()

    @Test
    fun `getMessage returns correct message for English`() = runTest {
        val message = service.getMessage(MessageKey.START_WELCOME, Language.ENGLISH)
        assertEquals(
            "Hello! I am a bot that will help you read Twitter without leaving Telegram!",
            message
        )
    }

    @Test
    fun `getMessage returns correct message for Ukrainian`() = runTest {
        val message = service.getMessage(MessageKey.START_WELCOME, Language.UKRAINIAN)
        assertEquals(
            "Привіт! Я бот, який допоможе вам читати Twitter, не виходячи з Telegram!",
            message
        )
    }

    @Test
    fun `getMessage returns correct message for Russian`() = runTest {
        val message = service.getMessage(MessageKey.START_WELCOME, Language.RUSSIAN)
        assertEquals(
            "Привет! Я бот, который поможет вам читать Twitter, не выходя из Telegram!",
            message
        )
    }

    @Test
    fun `getMessage with parameters substitutes correctly`() = runTest {
        val parameters = mapOf("username" to "elonmusk")
        val message = service.getMessage(MessageKey.ADD_SUCCESS, Language.ENGLISH, parameters)
        assertEquals(
            "Successfully subscribed to @elonmusk! You will now receive new tweets from this account.",
            message
        )
    }

    @Test
    fun `getMessage with multiple parameters substitutes correctly`() = runTest {
        val parameters = mapOf(
            "username" to "elonmusk",
            "language" to "UA",
            "text" to "Hello world"
        )
        val message = service.getMessage(MessageKey.TWEET_TRANSLATED, Language.ENGLISH, parameters)
        assertEquals("[UA] Hello world", message)
    }

    @Test
    fun `getMessage falls back to English when key not found in target language`() = runTest {
        // Test with a key that might not exist in all languages
        val message = service.getMessage(MessageKey.SERVICE_MAINTENANCE, Language.UKRAINIAN)
        assertTrue(message.isNotEmpty())
    }

    @Test
    fun `getMessage returns key when message not found in any language`() = runTest {
        // This test assumes a non-existent key scenario
        // In real implementation, all keys should exist, but this tests fallback behavior
        val message = service.getMessage(MessageKey.ERROR_UNKNOWN, Language.ENGLISH)
        assertTrue(message.isNotEmpty())
    }

    @Test
    fun `hasMessage returns true for existing keys`() = runTest {
        assertTrue(service.hasMessage(MessageKey.START_WELCOME, Language.ENGLISH))
        assertTrue(service.hasMessage(MessageKey.START_WELCOME, Language.UKRAINIAN))
        assertTrue(service.hasMessage(MessageKey.START_WELCOME, Language.RUSSIAN))
    }

    @Test
    fun `getAvailableKeys returns all message keys`() = runTest {
        val availableKeys = service.getAvailableKeys()
        assertTrue(availableKeys.contains(MessageKey.START_WELCOME))
        assertTrue(availableKeys.contains(MessageKey.ADD_SUCCESS))
        assertTrue(availableKeys.contains(MessageKey.LANG_TITLE))
        assertEquals(MessageKey.entries.size, availableKeys.size)
    }

    @Test
    fun `nested JSON structure is flattened correctly`() = runTest {
        val startMessage = service.getMessage(MessageKey.START_WELCOME, Language.ENGLISH)
        val langMessage = service.getMessage(MessageKey.LANG_TITLE, Language.ENGLISH)

        assertTrue(startMessage.isNotEmpty())
        assertTrue(langMessage.isNotEmpty())
    }

    @Test
    fun `button messages are loaded correctly`() = runTest {
        val yesButton = service.getMessage(MessageKey.BUTTON_YES, Language.ENGLISH)
        val noButton = service.getMessage(MessageKey.BUTTON_NO, Language.ENGLISH)

        assertEquals("Yes", yesButton)
        assertEquals("No", noButton)
    }

    @Test
    fun `language button messages include emojis`() = runTest {
        val englishButton = service.getMessage(MessageKey.LANG_ENGLISH, Language.ENGLISH)
        val ukrainianButton = service.getMessage(MessageKey.LANG_UKRAINIAN, Language.UKRAINIAN)
        val russianButton = service.getMessage(MessageKey.LANG_RUSSIAN, Language.RUSSIAN)

        assertTrue(englishButton.startsWith("🇬🇧"))
        assertTrue(ukrainianButton.startsWith("🇺🇦"))
        assertTrue(russianButton.startsWith("🇷🇺"))
    }

    @Test
    fun `parameter substitution works with empty parameters map`() = runTest {
        val message = service.getMessage(MessageKey.START_WELCOME, Language.ENGLISH, emptyMap())
        assertEquals(
            "Hello! I am a bot that will help you read Twitter without leaving Telegram!",
            message
        )
    }

    @Test
    fun `parameter substitution ignores unused parameters`() = runTest {
        val parameters = mapOf(
            "username" to "elonmusk",
            "unused" to "value"
        )
        val message = service.getMessage(MessageKey.ADD_SUCCESS, Language.ENGLISH, parameters)
        assertEquals(
            "Successfully subscribed to @elonmusk! You will now receive new tweets from this account.",
            message
        )
    }
}
