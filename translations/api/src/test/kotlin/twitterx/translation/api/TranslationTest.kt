package twitterx.translation.api

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TranslationTest {

    @Test
    fun `Translation should store text and languages correctly`() {
        val text = "Hello world"
        val from = "en"
        val to = Language.UKRAINIAN

        val translation = Translation(text, to, from)

        assertEquals(text, translation.text)
        assertEquals(from, translation.from)
        assertEquals(to, translation.to)
    }

    @Test
    fun `isSameLanguage should return true for same languages`() {
        val translation = Translation(
            text = "Hello world",
            from = "en",
            to = Language.ENGLISH
        )

        assertTrue(translation.isSameLanguage())
    }

    @Test
    fun `isSameLanguage should return false for different languages`() {
        val translation = Translation(
            text = "Привіт світе",
            from = "en",
            to = Language.UKRAINIAN
        )

        assertFalse(translation.isSameLanguage())
    }

    @Test
    fun `isSameLanguage should return false for different language codes`() {
        val translation = Translation(
            text = "Hello world",
            from = "ru",
            to = Language.ENGLISH
        )

        assertFalse(translation.isSameLanguage())
    }

    @Test
    fun `isSameLanguage should return false for different languages same text`() {
        val translation = Translation(
            text = "Hello world",
            from = "ru",
            to = Language.UKRAINIAN
        )

        assertFalse(translation.isSameLanguage())
    }

    @Test
    fun `Translation should handle empty text`() {
        val translation = Translation(
            text = "",
            from = "en",
            to = Language.UKRAINIAN
        )

        assertEquals("", translation.text)
        assertEquals("en", translation.from)
        assertEquals(Language.UKRAINIAN, translation.to)
        assertFalse(translation.isSameLanguage())
    }

    @Test
    fun `Translation should handle text with newlines`() {
        val text = "Line 1\nLine 2\nLine 3"
        val translation = Translation(
            text = text,
            from = "en",
            to = Language.UKRAINIAN
        )

        assertEquals(text, translation.text)
        assertTrue(translation.text.contains("\n"))
    }

    @Test
    fun `Translation should handle text with special characters`() {
        val text = "Hello @user! Check this: https://example.com #hashtag"
        val translation = Translation(
            text = text,
            from = "en",
            to = Language.UKRAINIAN
        )

        assertEquals(text, translation.text)
        assertTrue(translation.text.contains("@user"))
        assertTrue(translation.text.contains("#hashtag"))
        assertTrue(translation.text.contains("https://example.com"))
    }

    @Test
    fun `Translation should handle text with emojis`() {
        val text = "Hello world! 😀🌍💫"
        val translation = Translation(
            text = text,
            from = "en",
            to = Language.UKRAINIAN
        )

        assertEquals(text, translation.text)
        assertTrue(translation.text.contains("😀"))
        assertTrue(translation.text.contains("🌍"))
        assertTrue(translation.text.contains("💫"))
    }

    @Test
    fun `Translation should handle long text`() {
        val longText = "This is a very long text that should be handled properly. ".repeat(100)
        val translation = Translation(
            text = longText,
            from = "en",
            to = Language.UKRAINIAN
        )

        assertEquals(longText, translation.text)
        assertTrue(translation.text.length > 1000)
    }

    @Test
    fun `Translation should work with different language codes`() {
        val translation = Translation(
            text = "Witaj świecie",
            from = "pl", // Polish
            to = Language.ENGLISH
        )

        assertEquals("Witaj świecie", translation.text)
        assertEquals("pl", translation.from)
        assertEquals(Language.ENGLISH, translation.to)
        assertFalse(translation.isSameLanguage())
    }

    @Test
    fun `Translation should handle Ukrainian language`() {
        val translation = Translation(
            text = "Привіт світе",
            from = "uk",
            to = Language.ENGLISH
        )

        assertEquals("Привіт світе", translation.text)
        assertEquals("uk", translation.from)
        assertEquals(Language.ENGLISH, translation.to)
        assertFalse(translation.isSameLanguage())
    }

    @Test
    fun `Translation should handle Russian language`() {
        val translation = Translation(
            text = "Привет мир",
            from = "ru",
            to = Language.ENGLISH
        )

        assertEquals("Привет мир", translation.text)
        assertEquals("ru", translation.from)
        assertEquals(Language.ENGLISH, translation.to)
        assertFalse(translation.isSameLanguage())
    }

    @Test
    fun `Translation equality should work correctly`() {
        val translation1 = Translation(
            text = "Hello world",
            from = "en",
            to = Language.UKRAINIAN
        )

        val translation2 = Translation(
            text = "Hello world",
            from = "en",
            to = Language.UKRAINIAN
        )

        assertEquals(translation1, translation2)
    }

    @Test
    fun `Translation should handle null-like strings`() {
        val translation = Translation(
            text = "null",
            from = "en",
            to = Language.UKRAINIAN
        )

        assertEquals("null", translation.text)
        assertEquals("en", translation.from)
        assertEquals(Language.UKRAINIAN, translation.to)
    }

    @Test
    fun `Language enum should have correct ISO codes`() {
        assertEquals("en", Language.ENGLISH.iso)
        assertEquals("uk", Language.UKRAINIAN.iso)
        assertEquals("ru", Language.RUSSIAN.iso)
    }

    @Test
    fun `isSameLanguage should work with all supported languages`() {
        // Test English
        val englishTranslation = Translation(
            text = "Hello world",
            from = "en",
            to = Language.ENGLISH
        )
        assertTrue(englishTranslation.isSameLanguage())

        // Test Ukrainian
        val ukrainianTranslation = Translation(
            text = "Привіт світе",
            from = "uk",
            to = Language.UKRAINIAN
        )
        assertTrue(ukrainianTranslation.isSameLanguage())

        // Test Russian
        val russianTranslation = Translation(
            text = "Привет мир",
            from = "ru",
            to = Language.RUSSIAN
        )
        assertTrue(russianTranslation.isSameLanguage())
    }

    @Test
    fun `Translation should handle mixed language codes`() {
        val translation = Translation(
            text = "Hello мир",
            from = "mixed",
            to = Language.ENGLISH
        )

        assertEquals("Hello мир", translation.text)
        assertEquals("mixed", translation.from)
        assertEquals(Language.ENGLISH, translation.to)
        assertFalse(translation.isSameLanguage())
    }

    @Test
    fun `Translation should handle unknown language codes`() {
        val translation = Translation(
            text = "Some text",
            from = "unknown",
            to = Language.ENGLISH
        )

        assertEquals("Some text", translation.text)
        assertEquals("unknown", translation.from)
        assertEquals(Language.ENGLISH, translation.to)
        assertFalse(translation.isSameLanguage())
    }
}
