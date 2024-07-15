package com.programistich.twitterx.features.dictionary

import com.programistich.twitterx.entities.ChatLanguage
import org.springframework.stereotype.Component
import org.tomlj.Toml
import org.tomlj.TomlParseResult
import org.tomlj.TomlTable

@Component
class Dictionary {
    private val nameFile = "dictionary.toml"

    private val resourceAsStream = checkNotNull(this::class.java.classLoader.getResourceAsStream(nameFile)) {
        "Toml file $nameFile is missing"
    }
    private val tomlParse: TomlParseResult = Toml.parse(resourceAsStream)
    private val cache: MutableMap<String, HashMap<String, String>> = hashMapOf()

    init {
        tomlParse.dottedKeySet().map { it.split(".").first() }.distinct().forEach { table ->
            val tomlTable: TomlTable = tomlParse.getTableOrEmpty(table)
            cache[table] = tomlTable.toMap() as HashMap<String, String>
        }
    }

    fun get(table: String, key: String, vararg values: String): String {
        val contentByTable = checkNotNull(cache[table]) { "Map by table $table is missing" }
        val textByKey = checkNotNull(contentByTable[key]) { "Map by table $table with key $key is missing" }
        if (values.isEmpty()) return textByKey.trimIndent()

        var textByReplaceValues = textByKey
        values.forEach {
            textByReplaceValues = textByReplaceValues.replaceFirst("{}", it)
        }
        return textByReplaceValues.trimIndent()
    }

    fun getByLang(table: String, language: ChatLanguage, vararg values: String): String {
        return get(table, language.name.lowercase(), *values)
    }
}
