package twitterx.article.telegraph

import org.slf4j.LoggerFactory
import twitterx.article.api.ArticleContentTooLongException
import twitterx.article.api.ArticleInvalidContentException

public class TextToDomConverter {
    private val logger = LoggerFactory.getLogger(TextToDomConverter::class.java)

    public companion object {
        private const val MAX_CONTENT_LENGTH = 64 * 1024 // 64KB as per Telegraph API
        private const val MAX_TITLE_LENGTH = 256
        private val URL_REGEX = """https?://[^\s]+""".toRegex()
    }

    public fun convertTextToNodes(text: String): List<TelegraphNode> {
        validateContent(text)

        logger.debug("Converting text to Telegraph nodes, length: ${text.length}")

        val paragraphs = text.split("\n\n")
            .filter { it.isNotBlank() }
            .map { it.trim() }

        if (paragraphs.isEmpty()) {
            throw ArticleInvalidContentException("No valid paragraphs found in content")
        }

        return paragraphs.map { paragraph ->
            val processedParagraph = processParagraph(paragraph)
            TelegraphNode.paragraph(processedParagraph)
        }
    }

    private fun validateContent(text: String) {
        when {
            text.length > MAX_CONTENT_LENGTH -> {
                throw ArticleContentTooLongException("Content exceeds maximum length of $MAX_CONTENT_LENGTH characters")
            }
            text.isBlank() -> {
                throw ArticleInvalidContentException("Content cannot be empty")
            }
        }
    }

    private fun processParagraph(paragraph: String): List<TelegraphNode> {
        val nodes = mutableListOf<TelegraphNode>()
        val currentText = paragraph

        // Process line breaks within paragraph
        val lines = currentText.split("\n")
        for (i in lines.indices) {
            val line = lines[i].trim()
            if (line.isNotEmpty()) {
                nodes.addAll(processLineContent(line))
            }
            // Add line break if not the last line
            if (i < lines.size - 1) {
                nodes.add(TelegraphNode.lineBreak())
            }
        }

        return nodes
    }

    private fun processLineContent(line: String): List<TelegraphNode> {
        val nodes = mutableListOf<TelegraphNode>()

        // Find URLs in the line
        val urls = URL_REGEX.findAll(line).toList()

        if (urls.isEmpty()) {
            // No URLs, just add as text
            nodes.add(TelegraphNode.text(line))
        } else {
            var lastIndex = 0

            for (match in urls) {
                // Add text before URL
                if (match.range.first > lastIndex) {
                    val textBefore = line.substring(lastIndex, match.range.first)
                    if (textBefore.isNotEmpty()) {
                        nodes.add(TelegraphNode.text(textBefore))
                    }
                }

                // Add URL as link
                val url = match.value
                nodes.add(TelegraphNode.link(url, listOf(TelegraphNode.text(url))))

                lastIndex = match.range.last + 1
            }

            // Add remaining text after last URL
            if (lastIndex < line.length) {
                val textAfter = line.substring(lastIndex)
                if (textAfter.isNotEmpty()) {
                    nodes.add(TelegraphNode.text(textAfter))
                }
            }
        }

        return nodes
    }

    public fun validateTitle(title: String): String {
        if (title.isBlank()) {
            throw ArticleInvalidContentException("Title cannot be empty")
        }

        if (title.length > MAX_TITLE_LENGTH) {
            throw ArticleContentTooLongException("Title exceeds maximum length of $MAX_TITLE_LENGTH characters")
        }

        return title.trim()
    }
}
