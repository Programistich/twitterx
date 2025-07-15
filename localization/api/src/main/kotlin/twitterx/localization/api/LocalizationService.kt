package twitterx.localization.api

import twitterx.translation.api.Language

/**
 * Service for providing localized messages in multiple languages.
 */
public interface LocalizationService {

    /**
     * Gets a localized message by key and language.
     *
     * @param key The message key
     * @param language The target language
     * @return The localized message, or the key if not found
     */
    public suspend fun getMessage(key: MessageKey, language: Language): String

    /**
     * Gets a localized message with parameter substitution.
     *
     * @param key The message key
     * @param language The target language
     * @param parameters Map of parameter names to values for substitution
     * @return The localized message with parameters substituted
     */
    public suspend fun getMessage(
        key: MessageKey,
        language: Language,
        parameters: Map<String, String>
    ): String

    /**
     * Gets all available message keys.
     *
     * @return Set of all message keys
     */
    public suspend fun getAvailableKeys(): Set<MessageKey>

    /**
     * Checks if a message exists for the given key and language.
     *
     * @param key The message key
     * @param language The target language
     * @return true if message exists, false otherwise
     */
    public suspend fun hasMessage(key: MessageKey, language: Language): Boolean
}
