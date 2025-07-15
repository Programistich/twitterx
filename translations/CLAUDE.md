# Translation Module

This module provides comprehensive text translation capabilities for the TwitterX project, enabling automatic translation of tweets and other content across multiple languages.

## Module Structure

The Translation module is organized into several sub-modules:

- **translations:api** - Core interfaces and models for translation services
- **translations:google** - Google Translate implementation using the unofficial API
- **translations:e2e** - End-to-end tests with real translation services

## Translation API (translations:api)

Core interfaces and models for translation functionality.

### Key Components

#### TranslationService Interface
Main interface for translation operations with a single method:

```kotlin
public interface TranslationService {
    public suspend fun translate(
        text: String,
        to: Language,
    ): Result<Translation>
}
```

#### Translation Data Class
Result container for translation operations:

```kotlin
public data class Translation(
    val text: String,        // Translated text
    val to: Language,        // Target language
    val from: String,        // Source language (detected automatically)
) {
    public fun isSameLanguage(): Boolean // Checks if source and target are the same
}
```

#### Language Enumeration
Supported languages with their ISO codes:

```kotlin
public enum class Language(public val iso: String) {
    ENGLISH("en"),
    UKRAINIAN("uk"),
    RUSSIAN("ru"),
}
```

## Google Translate Implementation (translations:google)

Google Translate service implementation using the unofficial Google Translate API.

### Key Components

#### GoogleTranslationService
Main service implementation that uses Google's free translation API:

- **Endpoint**: `https://translate.google.com/translate_a/single`
- **Method**: GET request with query parameters
- **Features**:
  - Automatic source language detection (`sl=auto`)
  - Comprehensive logging for debugging
  - Error handling with Result wrapper
  - JSON response parsing

#### Request Parameters
- `client=gtx` - Client type
- `sl=auto` - Automatically detect source language
- `tl=${targetLanguage}` - Target language ISO code
- `dt=t` - Return translation
- `dt=bd` - Return additional data
- `dj=1` - Return JSON format
- `q=${text}` - Text to translate

#### GoogleTranslateResponse
Internal data models for parsing Google's API response:

```kotlin
@Serializable
internal data class GoogleTranslateResponse(
    val sentences: List<Sentence>,
    val src: String,  // Detected source language
)

@Serializable
internal data class Sentence(
    val text: String,  // Translated text
    val from: String,  // Original text segment
)
```

### Error Handling

The service provides comprehensive error handling:
- Network failures are caught and logged
- Invalid responses are handled gracefully
- Returns `Result<Translation>` for safe error propagation
- Detailed logging for debugging translation issues

### Usage Example

```kotlin
val httpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json()
    }
}

val translationService = GoogleTranslationService(httpClient)

val result = translationService.translate("Hello, world!", Language.UKRAINIAN)
    .getOrNull()

// Result: Translation(text="Привіт, світ!", to=UKRAINIAN, from="en")
```

## End-to-End Testing (translations:e2e)

Comprehensive test suite for translation functionality validation.

### Test Cases

The E2E module covers:

- **Basic Translation**: English to Ukrainian translation
- **Same Language Detection**: Handling when source and target languages are identical
- **Error Scenarios**: Network failures and invalid responses
- **Language Detection**: Automatic source language identification

### Test Examples

```kotlin
@Test
fun `test translate from uk to en`() = runTest {
    val result = service.translate("Hello, world!", Language.UKRAINIAN).getOrThrow()
    assertTrue(result.from == "en")
    assertTrue(result.to.iso == Language.UKRAINIAN.iso)
    assertTrue(result.text == "Привіт, світ!")
}

@Test
fun `test translate same`() = runTest {
    val result = service.translate("Hello, world!", Language.ENGLISH).getOrThrow()
    assertTrue(result.isSameLanguage())
}
```

## Configuration and Dependencies

### HTTP Client Configuration
The module uses Ktor HTTP client with:
- **Engine**: CIO for cross-platform compatibility
- **Content Negotiation**: JSON serialization support
- **Lenient Parsing**: Handles unknown JSON fields gracefully

### Dependencies
- **Ktor Client**: HTTP requests and JSON parsing
- **Kotlinx Serialization**: JSON response deserialization
- **SLF4J**: Logging framework
- **Kotlin Coroutines**: Asynchronous operations

## Integration with TwitterX

The translation module integrates with the main TwitterX application to:
- Translate tweets based on user's preferred language
- Provide automatic language detection for incoming content
- Support the `/lang` command functionality
- Enable multilingual user interfaces

## Testing

### Unit Tests
```bash
./gradlew :translations:google:test
```

### End-to-End Tests
```bash
./gradlew :translations:e2e:test
```

## API Limitations

### Google Translate API
- **Rate Limits**: Unofficial API may have usage restrictions
- **Availability**: Service availability depends on Google's policies
- **Language Support**: Limited to the three configured languages (EN, UK, RU)
- **Text Length**: No explicit limits but large texts may fail
