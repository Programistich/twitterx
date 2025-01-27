package com.programistich.twitterx.features.translate

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GoogleTranslateResponse(
    @SerialName("sentences") val sentences: List<Sentence>,
) {
    @Serializable
    data class Sentence(
        @SerialName("trans") val trans: String,
        @SerialName("orig") val orig: String,
    )
}
