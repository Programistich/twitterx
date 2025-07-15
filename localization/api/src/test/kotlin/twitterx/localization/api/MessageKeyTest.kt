package twitterx.localization.api

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MessageKeyTest {

    @Test
    fun `all message keys have non-empty key strings`() {
        MessageKey.entries.forEach { messageKey ->
            assertNotNull(messageKey.key, "Message key should not be null: $messageKey")
            assertTrue(messageKey.key.isNotBlank(), "Message key should not be blank: $messageKey")
        }
    }

    @Test
    fun `all message keys have unique key strings`() {
        val keyStrings = MessageKey.entries.map { it.key }
        val uniqueKeyStrings = keyStrings.toSet()

        assertTrue(
            keyStrings.size == uniqueKeyStrings.size,
            "All message keys should have unique key strings. Duplicates found."
        )
    }

    @Test
    fun `message keys follow dot notation convention`() {
        MessageKey.entries.forEach { messageKey ->
            assertTrue(
                messageKey.key.contains("."),
                "Message key should follow dot notation: ${messageKey.key}"
            )
        }
    }

    @Test
    fun `essential command message keys exist`() {
        val essentialKeys = listOf(
            MessageKey.START_WELCOME,
            MessageKey.ADD_SUCCESS,
            MessageKey.REMOVE_SUCCESS,
            MessageKey.LIST_TITLE,
            MessageKey.LANG_TITLE
        )

        essentialKeys.forEach { key ->
            assertNotNull(key, "Essential message key should exist: $key")
        }
    }
}
