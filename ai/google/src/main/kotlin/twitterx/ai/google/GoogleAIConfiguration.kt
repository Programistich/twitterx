package twitterx.ai.google

import kotlinx.serialization.Serializable

@Serializable
public data class GoogleAIConfiguration(
    public val secure1PSID: String,
    public val secure1PSIDTS: String,
    public val proxy: String? = null,
    public val timeout: Long = 30_000L,
    public val autoClose: Boolean = false,
    public val closeDelay: Long = 300_000L,
    public val autoRefresh: Boolean = true,
    public val refreshInterval: Long = 540_000L,
    public val pythonExecutable: String = "python3",
    public val scriptPath: String,
)
