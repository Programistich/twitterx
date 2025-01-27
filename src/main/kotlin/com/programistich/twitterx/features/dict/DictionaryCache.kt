package com.programistich.twitterx.features.dict

import com.programistich.twitterx.core.telegram.models.Language
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.tomlj.Toml
import org.tomlj.TomlParseResult
import org.tomlj.TomlTable

@Component
class DictionaryCache {

    private val cache: MutableMap<String, HashMap<String, String>> = hashMapOf()

    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReady() {
        extractAll()
        checkAllTranslate()
    }

    private fun extractAll() {
        val resourceAsStream = this::class.java.classLoader.getResourceAsStream("dictionary.toml")
        val tomlParse: TomlParseResult = Toml.parse(resourceAsStream)
        tomlParse.dottedKeySet().map { it.split(".").first() }.distinct().forEach { table ->
            val tomlTable: TomlTable = tomlParse.getTableOrEmpty(table)
            cache[table] = tomlTable.toMap() as HashMap<String, String>
        }
    }

    private fun checkAllTranslate() {
        val keys = DictionaryKey.entries.map { it.value }
        val languages = Language.entries.map { it.name.lowercase() }

        keys.forEach { key ->
            languages.forEach { language ->
                checkNotNull(getRaw(key, language)) { "Translate for key $key and language $language is missing" }
            }
        }
    }

    private fun getRaw(table: String, key: String, vararg values: String): String? {
        val contentByTable = cache[table] ?: return null
        val textByKey = contentByTable[key] ?: return null
        if (values.isEmpty()) return textByKey.trimIndent()

        var textByReplaceValues = textByKey
        values.forEach {
            textByReplaceValues = textByReplaceValues.replaceFirst("{}", it)
        }
        return textByReplaceValues.trimIndent()
    }

    fun getByKey(key: DictionaryKey, language: Language, vararg values: String): String {
        return checkNotNull(getRaw(key.value, language.name.lowercase(), *values))
    }
}
