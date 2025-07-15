package twitterx.ai.api

/**
 * Base exception for all AI-related errors
 */
public sealed class AIException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Authentication error when accessing AI service
 */
public class AIAuthException(message: String, cause: Throwable? = null) : AIException(message, cause)

/**
 * API error from AI service
 */
public class AIApiException(message: String, cause: Throwable? = null) : AIException(message, cause)

/**
 * Timeout error during AI request
 */
public class AITimeoutException(message: String, cause: Throwable? = null) : AIException(message, cause)

/**
 * Usage limit exceeded for AI model
 */
public class AIUsageLimitException(message: String, cause: Throwable? = null) : AIException(message, cause)

/**
 * Invalid model specified
 */
public class AIModelInvalidException(message: String, cause: Throwable? = null) : AIException(message, cause)

/**
 * IP temporarily blocked by AI service
 */
public class AITemporarilyBlockedException(message: String, cause: Throwable? = null) : AIException(message, cause)

/**
 * Process execution error (for subprocess implementations)
 */
public class AIProcessException(message: String, cause: Throwable? = null) : AIException(message, cause)
