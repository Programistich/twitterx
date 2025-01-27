package com.programistich.twitterx.features.ytdlp

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class TikTokApi(
    private val httpClient: HttpClient,
    @Value("\${tik-tok-service.path}") private val apiUrl: String
) {
    suspend fun getContent(url: String): TiktokResponse {
        val response = httpClient.get("$apiUrl/tiktok") {
            parameter("url", url)
        }

        val jsonObject = response.body<JsonObject>()
        return when (val typeValue = jsonObject["type"]?.jsonPrimitive?.content) {
            "images" -> Json.decodeFromJsonElement<ImagesResponse>(jsonObject)
            "video" -> Json.decodeFromJsonElement<VideoResponse>(jsonObject)
            else -> throw IllegalStateException("Unknown response type: $typeValue")
        }
    }
}

@Serializable
sealed interface TiktokResponse {
    val type: String
}

@Serializable
@SerialName("images")
data class ImagesResponse(
    override val type: String,
    val photo: List<String>
) : TiktokResponse

@Serializable
@SerialName("video")
data class VideoResponse(
    override val type: String,
    val video: String
) : TiktokResponse
