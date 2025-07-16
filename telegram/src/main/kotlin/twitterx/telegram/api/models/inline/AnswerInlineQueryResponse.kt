package twitterx.telegram.api.models.inline

import kotlinx.serialization.Serializable

/**
 * Response model for answering inline queries.
 */
@Serializable
public data class AnswerInlineQueryResponse(
    val ok: Boolean,
    val result: Boolean? = null,
    val description: String? = null
)
