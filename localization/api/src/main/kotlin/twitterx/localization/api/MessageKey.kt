package twitterx.localization.api

/**
 * Enumeration of all message keys used in the TwitterX application.
 * Each key corresponds to a user-facing text that needs localization.
 */
public enum class MessageKey(public val key: String) {

    // Start command messages
    START_WELCOME("start.welcome"),
    START_INSTRUCTIONS("start.instructions"),

    // Add subscription command
    ADD_SUCCESS("add.success"),
    ADD_ALREADY_EXISTS("add.already_exists"),
    ADD_USER_NOT_FOUND("add.user_not_found"),
    ADD_INVALID_USERNAME("add.invalid_username"),
    ADD_ERROR("add.error"),

    // Remove subscription command
    REMOVE_SUCCESS("remove.success"),
    REMOVE_NOT_FOUND("remove.not_found"),
    REMOVE_ERROR("remove.error"),

    // List subscriptions command
    LIST_TITLE("list.title"),
    LIST_EMPTY("list.empty"),
    LIST_ERROR("list.error"),

    // Language selection command
    LANG_TITLE("lang.title"),
    LANG_SELECTED("lang.selected"),
    LANG_ERROR("lang.error"),

    // AI command messages
    AI_RESPONSE("ai.response"),
    AI_ERROR("ai.error"),
    AI_UNAVAILABLE("ai.unavailable"),
    AI_PROCESSING("ai.processing"),

    // Tweet notifications
    TWEET_FROM("tweet.from"),
    TWEET_RETWEET_BY("tweet.retweet_by"),
    TWEET_ORIGINAL("tweet.original"),
    TWEET_TRANSLATED("tweet.translated"),
    TWEET_ERROR("tweet.error"),

    // Video download
    VIDEO_DOWNLOADING("video.downloading"),
    VIDEO_SUCCESS("video.success"),
    VIDEO_ERROR("video.error"),
    VIDEO_UNSUPPORTED("video.unsupported"),
    VIDEO_TOO_LARGE("video.too_large"),
    VIDEO_PROCESSING("video.processing"),
    VIDEO_DOWNLOAD_ERROR("video.download_error"),
    VIDEO_FILE_TOO_LARGE("video.file_too_large"),
    VIDEO_PROCESSING_TIMEOUT("video.processing_timeout"),
    VIDEO_UNSUPPORTED_URL("video.unsupported_url"),
    VIDEO_CAPTION("video.caption"),

    // Inline query
    INLINE_NO_RESULTS("inline.no_results"),
    INLINE_ERROR("inline.error"),

    // Link processing
    LINK_PROCESSING("link.processing"),
    LINK_SUBSCRIBE_PROMPT("link.subscribe_prompt"),
    LINK_ERROR("link.error"),

    // General errors
    ERROR_UNKNOWN("error.unknown"),
    ERROR_API_FAIL("error.api_fail"),
    ERROR_NETWORK("error.network"),
    ERROR_RATE_LIMIT("error.rate_limit"),

    // UI elements
    BUTTON_YES("button.yes"),
    BUTTON_NO("button.no"),
    BUTTON_SUBSCRIBE("button.subscribe"),
    BUTTON_CANCEL("button.cancel"),

    // Language names for buttons
    LANG_ENGLISH("lang.english"),
    LANG_UKRAINIAN("lang.ukrainian"),
    LANG_RUSSIAN("lang.russian"),

    // Validation messages
    VALIDATION_REQUIRED("validation.required"),
    VALIDATION_INVALID_FORMAT("validation.invalid_format"),
    VALIDATION_TOO_LONG("validation.too_long"),

    // Service status
    SERVICE_UNAVAILABLE("service.unavailable"),
    SERVICE_MAINTENANCE("service.maintenance"),

    // Elon Musk subscription
    ELONMUSK_SUBSCRIBED("elonmusk.subscribed"),
    ELONMUSK_UNSUBSCRIBED("elonmusk.unsubscribed"),
}
