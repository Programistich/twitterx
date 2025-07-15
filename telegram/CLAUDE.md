# Telegram Module

This module provides comprehensive Telegram Bot API functionality for the TwitterX project, enabling bot interactions, message processing, and user interface management.

Telegram Official Documentation: [Telegram Bot API](https://core.telegram.org/bots/api)


### Key Components

#### Update Types

The module supports various types of Telegram updates through a sealed interface hierarchy:

```kotlin
sealed interface TelegramUpdate {
    fun chatId(): Long?
}
```

**Message Updates:**
- `TelegramMessageUpdate` - Regular text messages with command support
- `TelegramEditedMessageUpdate` - Edited message notifications
- `TelegramChannelPostUpdate` - Channel post notifications

**Interactive Updates:**
- `TelegramCallbackQueryUpdate` - Inline keyboard button presses
- `TelegramInlineQuery` - Inline bot queries

#### Command System

```kotlin
enum class TelegramCommand(val value: String) {
    START("/start"),
    ADD("/add"),
    REMOVE("/remove"),
    LIST("/list"),
    LANG("/lang"),
    GPT("/gpt")
}
```

Supports all TwitterX bot commands as specified in TASK.md:
- `/start` - Welcome message and instructions
- `/add @username` - Subscribe to Twitter account
- `/remove @username` - Unsubscribe from Twitter account
- `/list` - Show active subscriptions
- `/lang` - Language selection interface

#### Executor Pattern

The module implements the Chain of Responsibility pattern for message processing:

```kotlin
interface Executor<UPDATE : TelegramUpdate> {
    val priority: Priority
    suspend fun canProcess(context: TelegramContext<UPDATE>): Boolean
    suspend fun process(context: TelegramContext<UPDATE>)
}
```

**Specialized Executors:**
- `CommandExecutor` - Abstract base for command handling with template method pattern
- `CallbackQueryExecutor<T>` - Type-safe callback query processing with JSON deserialization
- `InlineQueryExecutor` - Inline query processing

**Priority System:**
- `HIGH` - Commands and callback queries (immediate response)
- `MEDIUM` - Regular message processing
- `LOW` - Background tasks and notifications

#### Context Object

Rich context object for update processing:

```kotlin
data class TelegramContext<UPDATE : TelegramUpdate>(
    val update: UPDATE,
    val config: TelegramConfig,
    val userId: Long,
    val userLanguage: Language? = null
)
```
