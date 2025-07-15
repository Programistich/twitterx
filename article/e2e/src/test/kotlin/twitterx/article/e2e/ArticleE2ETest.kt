package twitterx.article.e2e

import kotlinx.coroutines.test.runTest
import twitterx.article.api.ArticleService
import twitterx.article.telegraph.TelegraphService
import kotlin.test.Test
import kotlin.test.assertTrue

class ArticleE2ETest {

    @Test
    fun `should create article with simple text`() = runTest {
        val service: ArticleService = TelegraphService()

        val text = """
            This is a test article created by TwitterX.
            
            It contains multiple paragraphs to test the Telegraph API integration.
            
            This paragraph has a link: https://github.com/anthropics/claude-code
            
            And this is the final paragraph.
        """.trimIndent()

        val result = service.createArticle(text, "Test Article - Simple Text")

        assertTrue(result.isSuccess, "Article creation should succeed")
        val url = result.getOrNull()
        assertTrue(url?.startsWith("https://telegra.ph/") == true, "Should return Telegraph URL")

        println("Created article: $url")
    }

    @Test
    fun `should create article with complex formatting`() = runTest {
        val service: ArticleService = TelegraphService()

        val text = """
            TwitterX Article Module Test
            
            This is a comprehensive test of the article creation functionality.
            
            Features tested:
            - Multiple paragraphs
            - Line breaks within paragraphs
            First line
            Second line
            Third line
            
            URL handling:
            Visit https://example.com for more information.
            Also check out https://github.com and https://google.com for reference.
            
            Special characters and Unicode:
            Testing special characters: éñüñ, 中文, العربية, русский
            
            Long content test:
            ${"Lorem ipsum dolor sit amet, consectetur adipiscing elit. ".repeat(10)}
            
            End of test article.
        """.trimIndent()

        val result = service.createArticle(text, "TwitterX Test Article - Complex Formatting")

        assertTrue(result.isSuccess, "Complex article creation should succeed")
        val url = result.getOrNull()
        assertTrue(url?.startsWith("https://telegra.ph/") == true, "Should return Telegraph URL")

        println("Created complex article: $url")
    }

    @Test
    fun `should handle long article content`() = runTest {
        val service: ArticleService = TelegraphService()

        val paragraphs = (1..20).map { i ->
            "This is paragraph $i. ".repeat(10) + "It contains a lot of text to test the handling of longer articles."
        }

        val text = paragraphs.joinToString("\n\n")

        val result = service.createArticle(text, "Long Article Test")

        assertTrue(result.isSuccess, "Long article creation should succeed")
        val url = result.getOrNull()
        assertTrue(url?.startsWith("https://telegra.ph/") == true, "Should return Telegraph URL")

        println("Created long article: $url")
    }

    @Test
    fun `should handle article with only URLs`() = runTest {
        val service: ArticleService = TelegraphService()

        val text = """
            https://example.com
            
            https://github.com
            
            https://google.com
            
            https://stackoverflow.com
        """.trimIndent()

        val result = service.createArticle(text, "URL Collection Test")

        assertTrue(result.isSuccess, "URL-only article creation should succeed")
        val url = result.getOrNull()
        assertTrue(url?.startsWith("https://telegra.ph/") == true, "Should return Telegraph URL")

        println("Created URL collection article: $url")
    }

    @Test
    fun `should handle article with mixed content`() = runTest {
        val service: ArticleService = TelegraphService()

        val text = """
            TwitterX Bot Article Creation Test
            
            This article demonstrates the capability of the TwitterX bot to create articles from tweet content.
            
            Key features:
            - Automatic paragraph detection
            - URL conversion to clickable links
            - Support for Unicode characters
            - Line break handling
            
            Example links:
            Official documentation: https://docs.anthropic.com
            GitHub repository: https://github.com/anthropics/claude-code
            
            Technical details:
            The article module uses the Telegraph API to create articles.
            It converts plain text to Telegraph's DOM node structure.
            Each paragraph is processed separately.
            URLs are automatically detected and converted to links.
            
            This is particularly useful for long tweets or tweet threads
            that exceed Telegram's message length limits.
            
            The system is designed to be reliable and handle various edge cases.
        """.trimIndent()

        val result = service.createArticle(text, "TwitterX Bot - Article Creation Demo")

        assertTrue(result.isSuccess, "Mixed content article creation should succeed")
        val url = result.getOrNull()
        assertTrue(url?.startsWith("https://telegra.ph/") == true, "Should return Telegraph URL")

        println("Created mixed content article: $url")
    }
}
