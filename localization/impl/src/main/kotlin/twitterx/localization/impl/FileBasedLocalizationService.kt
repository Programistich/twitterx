package twitterx.localization.impl

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.slf4j.LoggerFactory
import twitterx.localization.api.InvalidMessageFormatException
import twitterx.localization.api.LocalizationService
import twitterx.localization.api.MessageFileLoadException
import twitterx.localization.api.MessageKey
import twitterx.localization.api.ParameterSubstitutionException
import twitterx.translation.api.Language

/**
 * File-based implementation of LocalizationService that loads messages from JSON files.
 */
public class FileBasedLocalizationService : LocalizationService {

    private val logger = LoggerFactory.getLogger(FileBasedLocalizationService::class.java)
    private val json = Json { ignoreUnknownKeys = true }
    private val messagesCache = mutableMapOf<Language, Map<String, String>>()
    private val mutex = Mutex()

    public override suspend fun getMessage(key: MessageKey, language: Language): String {
        return getMessage(key, language, emptyMap())
    }

    public override suspend fun getMessage(
        key: MessageKey,
        language: Language,
        parameters: Map<String, String>
    ): String {
        return try {
            val messages = loadMessages(language)
            val message = messages[key.key] ?: run {
                logger.warn("Message key '${key.key}' not found for language $language, falling back to English")
                val englishMessages = loadMessages(Language.ENGLISH)
                englishMessages[key.key] ?: key.key
            }

            substituteParameters(message, parameters, key.key)
        } catch (e: Exception) {
            logger.error("Failed to get message for key '${key.key}' and language $language", e)
            key.key
        }
    }

    public override suspend fun getAvailableKeys(): Set<MessageKey> {
        return MessageKey.entries.toSet()
    }

    public override suspend fun hasMessage(key: MessageKey, language: Language): Boolean {
        return try {
            val messages = loadMessages(language)
            messages.containsKey(key.key)
        } catch (e: Exception) {
            logger.error("Failed to check if message exists for key '${key.key}' and language $language", e)
            false
        }
    }

    private suspend fun loadMessages(language: Language): Map<String, String> {
        return mutex.withLock {
            messagesCache[language] ?: run {
                val fileName = "messages_${language.iso}.json"
                logger.debug("Loading messages from file: $fileName")

                val resourceStream = this::class.java.classLoader.getResourceAsStream(fileName)
                    ?: throw MessageFileLoadException(language.iso, IllegalArgumentException("Resource not found: $fileName"))

                try {
                    val jsonContent = resourceStream.bufferedReader().use { it.readText() }
                    val jsonObject = json.parseToJsonElement(jsonContent) as? JsonObject
                        ?: throw InvalidMessageFormatException(fileName, IllegalArgumentException("Root element is not a JSON object"))

                    val flatMessages = flattenJsonObject(jsonObject)
                    messagesCache[language] = flatMessages
                    logger.info("Loaded ${flatMessages.size} messages for language $language")
                    flatMessages
                } catch (e: Exception) {
                    throw MessageFileLoadException(language.iso, e)
                }
            }
        }
    }

    private fun flattenJsonObject(jsonObject: JsonObject, prefix: String = ""): Map<String, String> {
        val result = mutableMapOf<String, String>()

        for ((key, value) in jsonObject) {
            val fullKey = if (prefix.isEmpty()) key else "$prefix.$key"

            when {
                value is JsonObject -> {
                    result.putAll(flattenJsonObject(value, fullKey))
                }
                value.jsonPrimitive.isString -> {
                    result[fullKey] = value.jsonPrimitive.content
                }
                else -> {
                    logger.warn("Ignoring non-string value for key '$fullKey': $value")
                }
            }
        }

        return result
    }

    private fun substituteParameters(
        message: String,
        parameters: Map<String, String>,
        messageKey: String
    ): String {
        return try {
            var result = message
            for ((paramName, paramValue) in parameters) {
                result = result.replace("{$paramName}", paramValue)
            }
            result
        } catch (e: Exception) {
            throw ParameterSubstitutionException(messageKey, parameters.keys.joinToString(), e)
        }
    }
}
