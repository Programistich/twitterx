# Article Module

This module provides article creation capabilities for the TwitterX project, enabling the creation of articles from text and titles using various publishing platforms.

## Module Structure

The Article module is organized into several sub-modules:

- **article:api** - Core interfaces and models for article creation
- **article:telegraph** - Telegraph API implementation for publishing articles
- **article:e2e** - End-to-end tests for article functionality

## Article API (article:api)

Core interfaces and models for article creation functionality.

### Key Components

#### ArticleService Interface
Main interface for article operations with a single method:

```kotlin
public interface ArticleService {
    public suspend fun createArticle(text: String, title: String): Result<String>
}
```

#### ArticleException Hierarchy
Comprehensive exception hierarchy for error handling:

```kotlin
public sealed class ArticleException : Exception
public class ArticleCreationException : ArticleException  
public class ArticleApiException : ArticleException
public class ArticleContentTooLongException : ArticleException
public class ArticleInvalidContentException : ArticleException
```

## Telegraph Implementation (article:telegraph)

Telegraph service implementation that creates articles on the Telegraph platform (https://telegra.ph/).

### Key Components

#### TelegraphService
Main service implementation that implements the `ArticleService` interface:

- **Account Management**: Automatically creates and caches Telegraph accounts
- **Content Conversion**: Converts plain text to Telegraph's DOM-based format
- **HTTP Client**: Uses Ktor client with JSON serialization and logging
- **Error Handling**: Comprehensive error handling with Result wrapper

#### TelegraphClient
HTTP client wrapper for Telegraph API interactions:

- **Base URL**: `https://api.telegra.ph/`
- **Account Creation**: Creates new accounts for article publishing
- **Page Creation**: Creates articles using Telegraph's createPage API
- **Error Processing**: Handles API responses and error conditions

#### TextToDomConverter
Utility class for converting plain text to Telegraph's DOM format:

- **Text Validation**: Validates titles and content length
- **DOM Conversion**: Converts text to Telegraph's node-based structure
- **Content Splitting**: Handles long text by creating multiple paragraphs

#### Telegraph Models
Data classes for Telegraph API communication:

```kotlin
// Account management
data class TelegraphAccount(
    val shortName: String,
    val authorName: String?,
    val authorUrl: String?,
    val accessToken: String?
)

// Page creation
data class TelegraphPage(
    val path: String,
    val url: String,
    val title: String,
    val description: String
)

// Content structure
sealed class Node
data class NodeElement(
    val tag: String,
    val attrs: Map<String, String>? = null,
    val children: List<Node>? = null
) : Node
```

### Configuration

The Telegraph service uses default configuration:
- **Account Name**: Auto-generated with format "TwitterX-{UUID}"
- **Author**: "TwitterX Bot"
- **Timeouts**: 30s request, 10s connect
- **Content Limits**: Telegraph's 64KB content limit

### Usage Example

```kotlin
val articleService = TelegraphService()

val result = articleService.createArticle(
    text = "This is a long tweet that needs to be published as an article...",
    title = "Tweet Thread Article"
)

// Result: Success("https://telegra.ph/Tweet-Thread-Article-01-15")
```

### Error Handling

The service provides comprehensive error handling for:
- **Network failures**: Connection timeouts and API errors
- **Content validation**: Title length and content size limits
- **Account issues**: Telegraph account creation failures
- **Content conversion**: Text to DOM format conversion errors

## End-to-End Testing (article:e2e)

Comprehensive test suite for article creation functionality validation.

### Test Cases

The E2E module covers:
- **Article Creation**: Basic text to article conversion
- **Error Scenarios**: Invalid content and API failures
- **Content Limits**: Testing Telegraph's size restrictions
- **Account Management**: Telegraph account creation and reuse

## Integration with TwitterX

The article module integrates with the main TwitterX application to:
- Create articles from long tweets that exceed Telegram's message limits
- Provide formatted article URLs for sharing
- Handle tweet threads by combining them into single articles
- Support the long text publishing feature

## Testing

### Unit Tests
```bash
./gradlew :article:telegraph:test
```

### End-to-End Tests
```bash
./gradlew :article:e2e:test
```

## Dependencies

The module uses:
- **Ktor Client**: HTTP requests and JSON serialization
- **Kotlinx Serialization**: JSON processing for Telegraph API
- **SLF4J**: Logging framework
- **Kotlin Coroutines**: Asynchronous operations

## API Limitations

### Telegraph Platform
- **Content Size**: Maximum 64KB per article
- **Rate Limits**: Telegraph's API rate limiting applies
- **Account Limits**: No explicit limits but recommended to reuse accounts
- **Content Format**: Limited to Telegraph's supported HTML tags