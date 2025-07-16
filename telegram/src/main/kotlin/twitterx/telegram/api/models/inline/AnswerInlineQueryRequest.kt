package twitterx.telegram.api.models.inline

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request model for answering inline queries.
 */
@Serializable
public data class AnswerInlineQueryRequest(
    @SerialName("inline_query_id")
    val inlineQueryId: String,
    val results: List<InlineQueryResult>,
    @SerialName("cache_time")
    val cacheTime: Int? = null,
    @SerialName("is_personal")
    val isPersonal: Boolean? = null,
    @SerialName("next_offset")
    val nextOffset: String? = null
)
