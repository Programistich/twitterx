package twitterx.ai.google

import kotlinx.serialization.Serializable

/**
 * Command to send to Python Gemini script
 */
@Serializable
internal data class PythonCommand(
    val action: String,
    val prompt: String? = null,
    val model: String? = null,
    val files: List<String> = emptyList(),
    val conversationId: String? = null,
    val metadata: List<String>? = null,
    val secure1PSID: String? = null,
    val secure1PSIDTS: String? = null,
    val proxy: String? = null,
    val timeout: Long = 30000L
)

/**
 * Response from Python Gemini script
 */
@Serializable
internal data class PythonResponse(
    val success: Boolean,
    val text: String? = null,
    val thoughts: String? = null,
    val conversationId: String? = null,
    val metadata: List<String>? = null,
    val model: String? = null,
    val images: List<PythonImage> = emptyList(),
    val error: String? = null,
    val errorType: String? = null
)

/**
 * Image data from Python script
 */
@Serializable
internal data class PythonImage(
    val url: String,
    val title: String = "[Image]",
    val alt: String = "",
    val isGenerated: Boolean = false
)

/**
 * Python process execution result
 */
internal data class PythonResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String
)
