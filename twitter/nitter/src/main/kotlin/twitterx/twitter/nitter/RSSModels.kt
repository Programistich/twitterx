package twitterx.twitter.nitter

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

internal data class RSSFeed(
    val title: String,
    val description: String,
    val link: String,
    val lastBuildDate: LocalDateTime,
    val items: List<RSSItem>
)

internal data class RSSItem(
    val title: String,
    val description: String,
    val link: String,
    val pubDate: LocalDateTime,
    val guid: String,
    val creator: String? = null,
    val content: String? = null
)

@Serializable
internal data class RSSChannel(
    val title: String,
    val description: String,
    val link: String,
    val lastBuildDate: String? = null,
    val item: List<RSSItemXml> = emptyList()
)

@Serializable
internal data class RSSItemXml(
    val title: String,
    val description: String,
    val link: String,
    val pubDate: String,
    val guid: String,
    @SerialName("dc:creator") val creator: String? = null
)
