package twitterx.translation.impl

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class GoogleTranslateResponse(
    @SerialName("sentences") val sentences: List<Sentence>,
    @SerialName("src") val src: String,
) {
    @Serializable
    internal data class Sentence(
        @SerialName("trans") val text: String,
        @SerialName("orig") val from: String,
    )
}
