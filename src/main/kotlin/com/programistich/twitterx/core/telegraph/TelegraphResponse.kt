package com.programistich.twitterx.core.telegraph

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val ok: Boolean,
    val result: T? = null,
    val error: String? = null
)

@Serializable
data class CreateAccountRequest(
    @SerialName("short_name") val shortName: String,
    @SerialName("author_name") val authorName: String,
    @SerialName("author_url") val authorUrl: String
)

@Serializable
data class CreateAccountResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("short_name") val shortName: String,
    @SerialName("author_name") val authorName: String,
    @SerialName("author_url") val authorUrl: String
)

@Serializable
data class CreatePageRequest(
    @SerialName("access_token") val accessToken: String,
    @SerialName("title") val title: String,
    @SerialName("author_name") val authorName: String,
    @SerialName("return_content") val returnContent: Boolean = false,
    @SerialName("content") val content: List<NodeElement>,
)

@Serializable
data class NodeElement(
    @SerialName("tag") val tag: String,
    @SerialName("children") val children: List<String>
)

@Serializable
data class CreatePageResponse(
    @SerialName("url") val url: String
)
