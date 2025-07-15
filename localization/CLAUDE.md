# Localization Module

This module provides comprehensive localization capabilities for the TwitterX project, enabling multi-language support across English, Ukrainian, and Russian languages.

## Module Structure

The Localization module is organized into several sub-modules:

- **localization:api** - Core interfaces and models for localization services
- **localization:impl** - File-based implementation using JSON message files

## Localization API (localization:api)

Core interfaces and models for localization functionality.

### Key Components

#### LocalizationService Interface
Main interface for localization operations:

```kotlin
public interface LocalizationService {
    public suspend fun getMessage(key: MessageKey, language: Language): String
    public suspend fun getMessage(key: MessageKey, language: Language, parameters: Map<String, String>): String
    public suspend fun getAvailableKeys(): Set<MessageKey>
    public suspend fun hasMessage(key: MessageKey, language: Language): Boolean
}
```

#### MessageKey Enumeration
Comprehensive enumeration of all user-facing text keys:

```kotlin
public enum class MessageKey(public val key: String) {
    START_WELCOME("start.welcome"),
    ADD_SUCCESS("add.success"),
    TWEET_FROM("tweet.from"),
    // ... 50+ message keys
}
```

#### Exception Hierarchy
Comprehensive exception handling for localization errors:
- `LocalizationException` - Base exception
- `MessageFileLoadException` - File loading errors
- `InvalidMessageFormatException` - JSON format errors
- `MessageKeyNotFoundException` - Missing message keys
- `ParameterSubstitutionException` - Parameter replacement errors

## File-Based Implementation (localization:impl)

JSON file-based implementation that loads messages from resource files.

### Key Features

#### Message Loading
- **File Format**: JSON files (`messages_en.json`, `messages_uk.json`, `messages_ru.json`)
- **Caching**: In-memory caching with thread-safe access using Mutex
- **Fallback**: Automatic fallback to English if message not found in target language
- **Nested Structure**: Support for nested JSON objects (e.g., `start.welcome`)

#### Parameter Substitution
Supports dynamic parameter replacement:

```kotlin
val parameters = mapOf("username" to "elonmusk")
val message = service.getMessage(MessageKey.ADD_SUCCESS, Language.ENGLISH, parameters)
// Result: "Successfully subscribed to @elonmusk! You will now receive new tweets from this account."
```

#### Error Handling
- Graceful degradation when messages are missing
- Comprehensive logging for debugging
- Returns message key as fallback when all else fails

### Message File Structure

```json
{
  "start": {
    "welcome": "Hello! I am a bot that will help you read Twitter without leaving Telegram!",
    "instructions": "Use /add @username to subscribe to a Twitter account..."
  },
  "add": {
    "success": "Successfully subscribed to @{username}! You will now receive new tweets from this account.",
    "already_exists": "You are already subscribed to @{username}."
  }
}
```

## Supported Languages

### English (en)
- Complete message set for all functionality
- Primary language for fallback scenarios
- American English conventions

### Ukrainian (uk)
- Complete translation of all user-facing text
- Proper Ukrainian grammar and conventions
- Support for Cyrillic characters

### Russian (ru)
- Complete translation of all user-facing text
- Proper Russian grammar and conventions
- Support for Cyrillic characters

## Message Categories

### Command Messages
- `/start` - Welcome and instructions
- `/add` - Subscription management
- `/remove` - Unsubscription
- `/list` - Subscription listing
- `/lang` - Language selection

### Tweet Processing
- Tweet formatting and display
- Translation indicators
- Author attribution
- Error handling

### Video Processing
- Download progress
- Success/failure notifications
- Platform support messages
- Size limit warnings

### Error Messages
- Network errors
- API failures
- Rate limiting
- Unknown errors

### UI Elements
- Button labels
- Validation messages
- Service status
- Language selection buttons

## Integration with TwitterX

The localization module integrates with the main TwitterX application to:
- Provide multi-language user interfaces
- Support the `/lang` command functionality
- Enable localized error messages and notifications
- Support tweet formatting in user's preferred language

## Testing

### Unit Tests
```bash
./gradlew :localization:impl:test
```

### End-to-End Tests
```bash
./gradlew :localization:e2e:test
```

### API Tests
```bash
./gradlew :localization:api:test
```

## Configuration and Dependencies

### Dependencies
- **Translation API**: For Language enumeration
- **Kotlinx Serialization**: JSON parsing
- **Kotlinx Coroutines**: Asynchronous operations
- **SLF4J**: Logging framework

### Resource Files
Message files are located in `localization/impl/src/main/resources/`:
- `messages_en.json` - English messages
- `messages_uk.json` - Ukrainian messages
- `messages_ru.json` - Russian messages

## Usage Example

```kotlin
val localizationService = FileBasedLocalizationService()

// Simple message
val welcome = localizationService.getMessage(MessageKey.START_WELCOME, Language.UKRAINIAN)
// Result: "Привіт! Я бот, який допоможе вам читати Twitter, не виходячи з Telegram!"

// Message with parameters
val parameters = mapOf("username" to "elonmusk")
val success = localizationService.getMessage(MessageKey.ADD_SUCCESS, Language.ENGLISH, parameters)
// Result: "Successfully subscribed to @elonmusk! You will now receive new tweets from this account."

// Check message availability
val hasMessage = localizationService.hasMessage(MessageKey.TWEET_FROM, Language.RUSSIAN)
// Result: true
```

## Best Practices

1. **Always use MessageKey enum** - Never use raw string keys
2. **Provide meaningful parameters** - Use descriptive parameter names
3. **Handle missing messages gracefully** - The service provides fallbacks
4. **Test all languages** - Ensure messages work in all supported languages
5. **Keep messages concise** - Consider Telegram message length limits
6. **Use consistent terminology** - Maintain consistent language across messages