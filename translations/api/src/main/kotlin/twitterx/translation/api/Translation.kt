package twitterx.translation.api

public data class Translation(
    val text: String,
    val to: Language,
    val from: String, // Because we don't know the language of the text, we use a string for the source language
) {
    public fun isSameLanguage(): Boolean {
        return to.iso == from
    }
}

public enum class Language(public val iso: String) {
    ENGLISH("en"),
    UKRAINIAN("uk"),
    RUSSIAN("ru"),
}
