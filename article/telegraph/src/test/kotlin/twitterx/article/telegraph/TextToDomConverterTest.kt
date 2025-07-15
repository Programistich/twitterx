package twitterx.article.telegraph

import twitterx.article.api.ArticleContentTooLongException
import twitterx.article.api.ArticleInvalidContentException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TextToDomConverterTest {

    private val converter = TextToDomConverter()

    @Test
    fun `should convert simple text to paragraph nodes`() {
        val text = "Hello world"
        val nodes = converter.convertTextToNodes(text)

        assertEquals(1, nodes.size)
        assertEquals("p", nodes[0].tag)
        assertEquals(1, nodes[0].children?.size)
        assertEquals("Hello world", nodes[0].children?.get(0)?.text)
    }

    @Test
    fun `should handle multiple paragraphs`() {
        val text = "First paragraph\n\nSecond paragraph"
        val nodes = converter.convertTextToNodes(text)

        assertEquals(2, nodes.size)
        assertEquals("p", nodes[0].tag)
        assertEquals("p", nodes[1].tag)
        assertEquals("First paragraph", nodes[0].children?.get(0)?.text)
        assertEquals("Second paragraph", nodes[1].children?.get(0)?.text)
    }

    @Test
    fun `should handle line breaks within paragraphs`() {
        val text = "Line 1\nLine 2"
        val nodes = converter.convertTextToNodes(text)

        assertEquals(1, nodes.size)
        val paragraph = nodes[0]
        assertEquals("p", paragraph.tag)
        assertEquals(3, paragraph.children?.size)

        assertEquals("Line 1", paragraph.children?.get(0)?.text)
        assertEquals("br", paragraph.children?.get(1)?.tag)
        assertEquals("Line 2", paragraph.children?.get(2)?.text)
    }

    @Test
    fun `should convert URLs to links`() {
        val text = "Visit https://example.com for more info"
        val nodes = converter.convertTextToNodes(text)

        assertEquals(1, nodes.size)
        val paragraph = nodes[0]
        assertEquals(3, paragraph.children?.size)

        assertEquals("Visit ", paragraph.children?.get(0)?.text)
        assertEquals("a", paragraph.children?.get(1)?.tag)
        assertEquals("https://example.com", paragraph.children?.get(1)?.attrs?.get("href"))
        assertEquals(" for more info", paragraph.children?.get(2)?.text)
    }

    @Test
    fun `should handle multiple URLs in text`() {
        val text = "Visit https://example.com and https://google.com"
        val nodes = converter.convertTextToNodes(text)

        assertEquals(1, nodes.size)
        val paragraph = nodes[0]
        assertEquals(4, paragraph.children?.size)

        assertEquals("Visit ", paragraph.children?.get(0)?.text)
        assertEquals("a", paragraph.children?.get(1)?.tag)
        assertEquals("https://example.com", paragraph.children?.get(1)?.attrs?.get("href"))
        assertEquals(" and ", paragraph.children?.get(2)?.text)
        assertEquals("a", paragraph.children?.get(3)?.tag)
        assertEquals("https://google.com", paragraph.children?.get(3)?.attrs?.get("href"))
    }

    @Test
    fun `should throw exception for empty content`() {
        assertFailsWith<ArticleInvalidContentException> {
            converter.convertTextToNodes("")
        }
    }

    @Test
    fun `should throw exception for blank content`() {
        assertFailsWith<ArticleInvalidContentException> {
            converter.convertTextToNodes("   \n\n   ")
        }
    }

    @Test
    fun `should throw exception for content too long`() {
        val longText = "a".repeat(65537) // 64KB + 1
        assertFailsWith<ArticleContentTooLongException> {
            converter.convertTextToNodes(longText)
        }
    }

    @Test
    fun `should validate title correctly`() {
        val validTitle = "This is a valid title"
        assertEquals(validTitle, converter.validateTitle(validTitle))
    }

    @Test
    fun `should trim title whitespace`() {
        val title = "  Title with spaces  "
        assertEquals("Title with spaces", converter.validateTitle(title))
    }

    @Test
    fun `should throw exception for empty title`() {
        assertFailsWith<ArticleInvalidContentException> {
            converter.validateTitle("")
        }
    }

    @Test
    fun `should throw exception for blank title`() {
        assertFailsWith<ArticleInvalidContentException> {
            converter.validateTitle("   ")
        }
    }

    @Test
    fun `should throw exception for title too long`() {
        val longTitle = "a".repeat(300)
        assertFailsWith<ArticleContentTooLongException> {
            converter.validateTitle(longTitle)
        }
    }
}
