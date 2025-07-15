package twitterx.article.telegraph

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
public data class TelegraphResponse<T>(
    val ok: Boolean,
    val result: T? = null,
    val error: String? = null
)

@Serializable
public data class TelegraphAccount(
    @SerialName("short_name")
    val shortName: String,
    @SerialName("author_name")
    val authorName: String? = null,
    @SerialName("author_url")
    val authorUrl: String? = null,
    @SerialName("access_token")
    val accessToken: String? = null,
    @SerialName("auth_url")
    val authUrl: String? = null,
    @SerialName("page_count")
    val pageCount: Int? = null
)

@Serializable
public data class TelegraphPage(
    val path: String,
    val url: String,
    val title: String,
    val description: String? = null,
    @SerialName("author_name")
    val authorName: String? = null,
    @SerialName("author_url")
    val authorUrl: String? = null,
    @SerialName("image_url")
    val imageUrl: String? = null,
    val content: List<TelegraphNode>? = null,
    val views: Int? = null,
    @SerialName("can_edit")
    val canEdit: Boolean? = null
)

@Serializable
public data class TelegraphNode(
    val tag: String? = null,
    val attrs: Map<String, String>? = null,
    val children: List<TelegraphNode>? = null,
    val text: String? = null
) {
    public companion object {
        public fun text(content: String): TelegraphNode = TelegraphNode(text = content)

        public fun element(
            tag: String,
            attrs: Map<String, String>? = null,
            children: List<TelegraphNode>? = null
        ): TelegraphNode = TelegraphNode(tag = tag, attrs = attrs, children = children)

        public fun paragraph(children: List<TelegraphNode>): TelegraphNode =
            element("p", children = children)

        public fun lineBreak(): TelegraphNode = element("br")

        public fun bold(children: List<TelegraphNode>): TelegraphNode =
            element("strong", children = children)

        public fun italic(children: List<TelegraphNode>): TelegraphNode =
            element("em", children = children)

        public fun link(href: String, children: List<TelegraphNode>): TelegraphNode =
            element("a", attrs = mapOf("href" to href), children = children)
    }

    public fun toJsonElement(): JsonElement {
        return if (text != null) {
            JsonPrimitive(text)
        } else {
            buildJsonObject {
                tag?.let { put("tag", it) }
                attrs?.let { attrs ->
                    put(
                        "attrs",
                        buildJsonObject {
                            attrs.forEach { (key, value) -> put(key, value) }
                        }
                    )
                }
                children?.let { children ->
                    put("children", JsonArray(children.map { it.toJsonElement() }))
                }
            }
        }
    }
}

@Serializable
public data class CreateAccountRequest(
    @SerialName("short_name")
    val shortName: String,
    @SerialName("author_name")
    val authorName: String? = null,
    @SerialName("author_url")
    val authorUrl: String? = null
)

@Serializable
public data class CreatePageRequest(
    @SerialName("access_token")
    val accessToken: String,
    val title: String,
    val content: List<TelegraphNode>,
    @SerialName("author_name")
    val authorName: String? = null,
    @SerialName("author_url")
    val authorUrl: String? = null,
    @SerialName("return_content")
    val returnContent: Boolean = false
)
