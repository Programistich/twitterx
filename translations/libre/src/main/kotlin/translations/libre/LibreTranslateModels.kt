package translations.libre

import kotlinx.serialization.Serializable

@Serializable
internal data class LibreTranslateRequest(
    val q: String,
    val source: String = "auto",
    val target: String,
    val format: String = "text",
    val alternatives: Int = 3,
    val api_key: String = ""
)

@Serializable
internal data class LibreTranslateResponse(
    val translatedText: String,
    val detectedLanguage: DetectedLanguage,
    val alternatives: List<String> = emptyList()
)

@Serializable
internal data class DetectedLanguage(
    val confidence: Int,
    val language: String
)
