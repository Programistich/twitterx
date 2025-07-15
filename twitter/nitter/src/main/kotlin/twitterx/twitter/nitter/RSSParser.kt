package twitterx.twitter.nitter

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

internal class RSSParser {

    private val dateFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH)

    fun parseRSSFeed(rssXml: String): RSSFeed {
        val channel = parseChannel(rssXml)

        return RSSFeed(
            title = channel.title,
            description = channel.description,
            link = channel.link,
            lastBuildDate = parseDate(channel.lastBuildDate),
            items = channel.item.map { parseItem(it) }
        )
    }

    private fun parseChannel(xml: String): RSSChannel {
        val title = extractValue(xml, "title")
        val description = extractValue(xml, "description")
        val link = extractValue(xml, "link")
        val lastBuildDate = extractValue(xml, "lastBuildDate")

        val items = extractItems(xml)

        return RSSChannel(
            title = title,
            description = description,
            link = link,
            lastBuildDate = lastBuildDate,
            item = items
        )
    }

    private fun parseItem(item: RSSItemXml): RSSItem {
        return RSSItem(
            title = item.title,
            description = item.description,
            link = item.link,
            pubDate = parseDate(item.pubDate),
            guid = item.guid,
            creator = item.creator,
            content = cleanDescription(item.description)
        )
    }

    private fun extractValue(xml: String, tag: String): String {
        val startTag = "<$tag>"
        val endTag = "</$tag>"
        val startIndex = xml.indexOf(startTag)
        val endIndex = xml.indexOf(endTag, startIndex)

        return if (startIndex != -1 && endIndex != -1) {
            xml.substring(startIndex + startTag.length, endIndex).trim()
        } else {
            ""
        }
    }

    private fun extractItems(xml: String): List<RSSItemXml> {
        val items = mutableListOf<RSSItemXml>()
        var currentIndex = 0

        while (true) {
            val itemStart = xml.indexOf("<item>", currentIndex)
            if (itemStart == -1) {
                break
            }

            val itemEnd = xml.indexOf("</item>", itemStart)
            if (itemEnd == -1) {
                break
            }

            val itemXml = xml.substring(itemStart, itemEnd + 7)
            val item = parseItemXml(itemXml)
            items.add(item)

            currentIndex = itemEnd + 7
        }

        return items
    }

    private fun parseItemXml(itemXml: String): RSSItemXml {
        return RSSItemXml(
            title = extractValue(itemXml, "title"),
            description = extractCDATA(itemXml, "description"),
            link = extractValue(itemXml, "link"),
            pubDate = extractValue(itemXml, "pubDate"),
            guid = extractValue(itemXml, "guid"),
            creator = extractValue(itemXml, "dc:creator")
        )
    }

    private fun extractCDATA(xml: String, tag: String): String {
        val startTag = "<$tag>"
        val endTag = "</$tag>"
        val startIndex = xml.indexOf(startTag)
        val endIndex = xml.indexOf(endTag, startIndex)

        if (startIndex == -1 || endIndex == -1) {
            return ""
        }

        val content = xml.substring(startIndex + startTag.length, endIndex)

        // Handle CDATA
        return if (content.startsWith("<![CDATA[") && content.endsWith("]]>")) {
            content.substring(9, content.length - 3)
        } else {
            content
        }.trim()
    }

    private fun parseDate(dateStr: String?): LocalDateTime {
        if (!dateStr.isNullOrBlank()) {
            return try {
                LocalDateTime.parse(dateStr, dateFormatter)
            } catch (e: Exception) {
                LocalDateTime.now()
            }
        }
        return LocalDateTime.now()
    }

    private fun cleanDescription(description: String): String {
        return description
            .replace(Regex("<[^>]*>"), "")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .trim()
    }
}
