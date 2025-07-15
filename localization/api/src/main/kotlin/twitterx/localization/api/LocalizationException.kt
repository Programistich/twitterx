package twitterx.localization.api

/**
 * Base exception for localization-related errors.
 */
public sealed class LocalizationException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Exception thrown when a message file cannot be loaded.
 */
public class MessageFileLoadException(
    language: String,
    cause: Throwable? = null
) : LocalizationException("Failed to load message file for language: $language", cause)

/**
 * Exception thrown when message file format is invalid.
 */
public class InvalidMessageFormatException(
    fileName: String,
    cause: Throwable? = null
) : LocalizationException("Invalid message file format: $fileName", cause)

/**
 * Exception thrown when a message key is not found.
 */
public class MessageKeyNotFoundException(
    key: String,
    language: String
) : LocalizationException("Message key '$key' not found for language: $language")

/**
 * Exception thrown when parameter substitution fails.
 */
public class ParameterSubstitutionException(
    key: String,
    parameterName: String,
    cause: Throwable? = null
) : LocalizationException("Failed to substitute parameter '$parameterName' in message '$key'", cause)
