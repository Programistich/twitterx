package com.programistich.twitterx.features.translate

import com.deepl.api.Translator
import com.programistich.twitterx.entities.ChatLanguage
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class TranslateService(@Value("\${deepl.key}") val apiKey: String) {
    private val translator = Translator(apiKey)

    fun translate(
        text: String,
        language: ChatLanguage,
    ): Result<TranslateModel> {
        return kotlin.runCatching {
            val to = convert(language)
            val result = translator.translateText(text, null, to)
            TranslateModel(
                text = result.text,
                to = to,
                from = result.detectedSourceLanguage
            )
        }
    }

    private fun convert(language: ChatLanguage): String {
        return when (language) {
            ChatLanguage.EN -> "en-US"
            ChatLanguage.UA -> language.iso
            ChatLanguage.RU -> language.iso
        }
    }
}
