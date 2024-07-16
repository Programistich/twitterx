package com.programistich.twitterx.features.telegraph
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class ApiResponse<T>(
    val ok: Boolean,
    val result: T? = null,
    val error: String? = null
)

@Serializable
data class Account(
    @SerialName("short_name") val shortName: String,
    @SerialName("author_name") val authorName: String,
    @SerialName("author_url") val authorUrl: String,
    @SerialName("access_token") val accessToken: String? = null,
    @SerialName("auth_url") val authUrl: String? = null,
    @SerialName("page_count") val pageCount: Int? = null
)

@Serializable
data class PageList(
    @SerialName("total_count") val totalCount: Int,
    @SerialName("pages") val pages: List<Page>
)

@Serializable
data class Page(
    @SerialName("url") val url: String
)

@Serializable
data class PageViews(
    val views: Int
)

@Serializable
data class NodeElement(
    val tag: String,
    val attrs: Map<String, String>? = null,
    val children: List<Node>? = null
)

@Serializable(with = NodeSerializer::class)
sealed class Node {
    @Serializable
    @SerialName("text")
    data class Text(val value: String) : Node()

    @Serializable
    @SerialName("element")
    data class Element(val element: NodeElement) : Node()
}

object NodeSerializer : KSerializer<Node> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Node") {
        element<String>("tag", isOptional = true)
        element<String>("value", isOptional = true)
        element<Map<String, String>>("attrs", isOptional = true)
        element<List<Node>>("children", isOptional = true)
    }

    override fun serialize(encoder: Encoder, value: Node) {
        when (value) {
            is Node.Text -> encoder.encodeString(value.value)
            is Node.Element -> encoder.encodeSerializableValue(NodeElement.serializer(), value.element)
        }
    }

    override fun deserialize(decoder: Decoder): Node {
        val input = decoder as? JsonDecoder
            ?: throw SerializationException("Expected Json Input for ${descriptor.serialName}")
        val jsonElement = input.decodeJsonElement()
        return if (jsonElement is JsonPrimitive) {
            Node.Text(jsonElement.content)
        } else {
            Node.Element(Json.decodeFromJsonElement(NodeElement.serializer(), jsonElement))
        }
    }
}
