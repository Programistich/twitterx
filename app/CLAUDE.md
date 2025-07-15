# Application Module

The Application module serves as the main entry point and integration layer for the TwitterX Telegram bot. It's a Spring Boot application that orchestrates all other modules to provide a complete bot functionality.

## Module Overview

The `app` module is the central application that:
- Integrates all TwitterX modules (Twitter, Translation, Video, Article, AI, Localization, Telegram)
- Provides the main Spring Boot application entry point
- Implements specific command executors for bot functionality
- Manages user data persistence through JPA repositories
- Handles Telegram webhook integration


## Key Features

### Tweet Message Formatting
The application implements a comprehensive tweet formatting system with the following features:

- **Header-First Layout**: All tweets display headers (with user info and links) at the top, followed by content
- **HTML Link Hiding**: Tweet URLs and user profile URLs are hidden using `<a>` tags for clean display
- **Username Normalization**: "@" symbols are automatically removed from usernames in display
- **Multi-language Support**: Automatic translation with language indicators (e.g., [UK], [EN])
- **Article Creation**: Long content automatically creates Telegraph articles to handle Telegram's message limits
- **Media Handling**: Supports photos, videos, and media groups with appropriate captions

### Message Structure
All tweet messages follow this consistent structure:
1. **Header**: Clickable links to tweet and user profile
2. **Content**: Translated content (if applicable) followed by original content
3. **Media**: Photos/videos attached separately

### Error Handling
If result not successful, send error message to user using reply 

## Architecture

```
app/
├── src/main/kotlin/twitter/app/
│   ├── TwitterXApplication.kt           # Main Spring Boot application
│   ├── SpringBootConfig.kt              # Spring configuration
│   ├── features/                        # Feature-specific command executors
│   │   ├── lang/                        # Language selection functionality
│   │   ├── start/                       # Welcome command
│   │   └── twitter/                     # Tweet processing
│   ├── repo/                           # Data persistence layer
│   └── telegram/                       # Telegram integration layer
├── src/main/resources/
│   ├── application.properties          # Spring Boot configuration
│   └── db/migration/                   # Database schema migrations
└── src/test/                          # Application tests
```

## Key Components

### Main Application

#### TwitterXApplication.kt
Spring Boot application entry point:
```kotlin
@SpringBootApplication
class TwitterXApplication

fun main(args: Array<String>) {
    runApplication<TwitterXApplication>(*args)
}
```

#### SpringBootConfig.kt
Central configuration for all Spring beans and module integrations:
- HTTP clients configuration
- Service bean declarations
- Cross-module dependency wiring
- Configuration properties

### Feature Commands

#### Language Selection (`features/lang/`)
Handles the `/lang` command for language switching:

- **LanguageCommandExecutor**: Processes `/lang` command and shows language selection keyboard
- **LanguageCallbackQuery**: Handles language selection from inline keyboard
- **LanguageCommandCallback**: Processes language selection and updates user preferences

#### Start Command (`features/start/`)
Handles the `/start` command for welcoming new users:

- **StartCommandExecutor**: Processes `/start` command and shows welcome message with instructions

#### Tweet Processing (`features/twitter/`)
Handles tweet link processing and formatting:

- **TweetMessageExecutor**: Processes messages containing Twitter links and formats them for display
- **TweetSender**: Core service for formatting and sending tweets with proper structure and localization
- **ElonMuskMonitoringService**: Monitors specific accounts for new tweets and sends them to subscribers

### Data Persistence

#### TelegramChatRepository
JPA repository for managing user data:
```kotlin
@Entity
@Table(name = "telegram_chat")
data class TelegramChat(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(name = "telegram_id", unique = true)
    val telegramId: Long,
    
    @Column(name = "language")
    @Enumerated(EnumType.STRING)
    val language: Language,
    
    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)
```

### Telegram Integration

#### TelegramClientImpl
Implementation of Telegram API client:
- Message sending capabilities
- File upload handling
- Inline keyboard management
- Error handling and retry logic

#### TelegramController
REST controller for handling Telegram webhooks:
- Webhook endpoint processing
- Update routing to appropriate executors
- Request/response logging

#### TelegramExecutorProcessor
Processes incoming Telegram updates using the Chain of Responsibility pattern:
- Command routing
- Callback query processing
- Context creation and management

#### TelegramUpdateConverter
Converts Telegram API updates to internal model format:
- Message parsing
- Media extraction
- User context creation

## Database Schema

### telegram_chat Table
```sql
CREATE TABLE telegram_chat (
    id BIGSERIAL PRIMARY KEY,
    telegram_id BIGINT NOT NULL UNIQUE,
    language VARCHAR(10) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Configuration

### Application Properties
```properties
# Server configuration
server.port=8080

# Database configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/twitterx
spring.datasource.username=${DB_USERNAME:twitterx}
spring.datasource.password=${DB_PASSWORD:password}

# JPA configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Telegram Bot configuration
telegram.bot.token=${TELEGRAM_BOT_TOKEN}
telegram.bot.username=${TELEGRAM_BOT_USERNAME}

# Flyway migration
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
```

## Integration Points

### Module Dependencies
The application module depends on:
- `twitter:impl` - Twitter functionality
- `translations:google` - Translation services
- `video:ytdlp` - Video processing
- `article:telegraph` - Article creation
- `ai:google` - AI capabilities
- `localization:impl` - Multi-language support
- `telegram` - Telegram Bot API

### Service Wiring
All services are wired together in `SpringBootConfig.kt`:
```kotlin
@Configuration
class SpringBootConfig {
    
    @Bean
    fun httpClient(): HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) { json() }
        install(Logging) { level = LogLevel.INFO }
    }
    
    @Bean
    fun twitterService(httpClient: HttpClient): TwitterService = 
        TwitterServiceImpl(
            fxTwitterService = FxTwitterService(httpClient),
            nitterService = NitterService(httpClient)
        )
    
    // ... other service beans
}
```

## Running the Application

### Development Mode
```bash
./gradlew :app:bootRun
```

### Environment Variables
Required environment variables:
- `TELEGRAM_BOT_TOKEN` - Telegram Bot API token
- `TELEGRAM_BOT_USERNAME` - Telegram bot username  
- `DB_USERNAME` - Database username
- `DB_PASSWORD` - Database password

## Tweet Formatting Implementation

### TweetSender Service
The `TweetSender` class is the core service responsible for formatting and sending tweets. Key features:

#### Message Formatting
- **Header Generation**: Uses localization service to create headers with hidden HTML links
- **Content Translation**: Integrates with translation service for multi-language support
- **Article Creation**: Automatically creates Telegraph articles for content exceeding Telegram limits
- **Media Processing**: Handles photos, videos, and media groups with proper captions

#### Tweet Types Support
- **Single Tweets**: Standard tweets with header, content, and optional media
- **Reply Threads**: Sends each tweet in sequence as replies to previous messages
- **Quote Tweets**: Sends original tweet first, then the quoting tweet
- **Retweets**: Special formatting showing retweeter info and original tweet link

#### Content Length Handling
- **Regular Messages**: Up to 4096 characters
- **Media Captions**: Up to 1024 characters  
- **Article Fallback**: Creates Telegraph articles for longer content
- **Header Exclusion**: When creating articles for media tweets, headers are excluded from article content

### Message Structure Examples

#### Single Tweet
```
Tweet from username

[UK] Переклад українською

[EN] Original English content
```

#### Retweet
```
Retweet username from tweet by originaluser

[UK] Переклад контенту

[EN] Original content
```

#### Media Tweet with Long Caption
```
Tweet from username

[Photo/Video attached]

https://telegra.ph/Tweet-01-15
```

### Localization Integration
All tweet headers and retweet messages use localized templates with hidden HTML links:
- `tweet.from` - Main tweet header template
- `tweet.retweet_by` - Retweet header template
- Templates support parameters for username, URLs, and tweet links

## Development Guidelines

### Adding New Commands
1. Create command executor in `features/` directory
2. Implement `CommandExecutor` interface
3. Add localization messages

### Adding New Services
1. Define service interface in appropriate module
2. Implement service class
3. Add service configuration in `SpringBootConfig`
4. Wire dependencies
5. Add integration tests

### Database Changes
1. Create Flyway migration in `db/migration/`
2. Update JPA entities
3. Update repository methods
4. Test migration compatibility
