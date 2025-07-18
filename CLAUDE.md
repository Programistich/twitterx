# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

TwitterX is a Telegram bot that provides comprehensive Twitter interaction capabilities without leaving Telegram. The project implements a modular architecture with clean separation of concerns across multiple specialized modules.

### Key References
- **@TASK.md** - Complete requirements and user stories
- **@architecture.mermaid** - System architecture diagram
- **Module Documentation**: Each module has detailed CLAUDE.md files

- **Dependency Management** -
    * Versions: Use @gradle/libs.versions.toml for all dependency versions
    * Plugins: Use aliases - alias(libs.plugins.kotlin.jvm) instead of id 'org.jetbrains.kotlin.jvm'
    * Libraries: Use catalog references - implementation(libs.spring.boot.starter) instead of implementation 'org.springframework.boot:spring-boot-starter'
    * Projects: Use typesafe accessors - implementation(projects.module) instead of implementation(project(':module'))

- **Code Quality** -
    * Comments: Avoid unnecessary comments - use self-explanatory naming
    * Use Kotlin Explicit API: everywhere add visibility modifiers (public/private/internal) to functions and properties
    * Constants: Use companion objects or top-level constants
        * Example: companion object { const val MY_CONSTANT = "value" } instead of val MY_CONSTANT = "value"
    * API Module not require tests: Do not include test dependencies in API modules, only in implementation modules

- **Testing Standards** -  
    * Unit Tests: testImplementation(libs.kotlin.test)
    * HTTP Mocking: testImplementation(libs.ktor.client.mock) for Ktor client tests
    * Execution: Use ./gradlew test in specific modules

- **MultiThreading** -  
    * Use coroutines for asynchronous operations
    * Avoid blocking calls in suspend functions
    * Use `Dispatchers.IO` for I/O operations
    * Use Mutex for shared mutable state
    * DO NOT use `runBlocking` in production code
    * DO NOT USE GLOBAL SCOPE - always use structured concurrency with `CoroutineScope`

## Architecture Overview

#### Twitter Module (`twitter/`)
Handles all Twitter-related functionality. See **@twitter/CLAUDE.md** for detailed information.

- **`twitter:api`** - Core interfaces and models for Twitter API interactions
- **`twitter:fx`** - Implementation using api.fxtwitter.com for scraping tweets by ID and username
- **`twitter:nitter`** - Implementation using nitter.net for scraping latest tweets from accounts
- **`twitter:impl`** - Main implementation combining fx and nitter modules
- **`twitter:e2e`** - End-to-end tests with real Twitter data

#### Translation Module (`translations/`)
Provides text translation capabilities across multiple providers. See **@translations/CLAUDE.md** for detailed information.

- **`translations:api`** - Core interfaces and models for translation services
- **`translations:google`** - Google Translate implementation using unofficial API
- **`translations:e2e`** - End-to-end tests with real translation services

#### Video Module (`video/`)
Handles video downloading from social networks. See **@video/CLAUDE.md** for details.

- **`video:api`** - Core interfaces and models for video operations
- **`video:ytdlp`** - Implementation using yt-dlp for video downloading
- **`video:e2e`** - End-to-end tests with real video sources

### Article Module (`article/`)
Create articles from text. See **@article/CLAUDE.md** for details.

- **`article:api`** - Core interfaces and models for article creation
- **`article:telegraph`** - Implementation using https://telegre.ph/ for article creation
- **`article:e2e`** - End-to-end tests with real article creation

### Localization Module (`localization/`)
Provides multi-language support for all user-facing text. See **@localization/CLAUDE.md** for details.

- **`localization:api`** - Core interfaces and models for localization services
- **`localization:impl`** - File-based implementation using JSON message files

### Telegram Module (`telegram/`)
Handles all Telegram Bot API functionality. See **@telegram/CLAUDE.md** for detailed information.

- **`telegram`** - Core interfaces and models for Telegram Bot API interactions

### AI Module (`ai/`)
Provides AI capabilities for the bot, including text generation and processing. See **@ai/CLAUDE.md** for details.

- **`ai:api`** - Core interfaces and models for AI services
- **`ai:google`** - Implementation using Google Gemini Web API via Python subprocess
- **`ai:e2e`** - End-to-end tests with real AI services

### Application Module (`app/`)
Main Spring Boot application that integrates all modules and provides executable bot functionality. See **@app/CLAUDE.md** for detailed information.

- **Main Application**: Spring Boot entry point with comprehensive configuration
- **Feature Commands**: Executors for `/start`, `/lang`, tweet processing
- **Data Persistence**: JPA repositories for TelegramChat (user language storage)
- **Telegram Integration**: Webhook controller, update processing, and client implementation
- **Service Wiring**: Complete integration of all modules into functional Telegram bot
- **Missing**: Subscription management, automatic monitoring, inline queries

> **Note**: Each module has its own structure and conventions - refer to module-specific documentation.

## Build Verification Process

**⚠️ CRITICAL**: After ANY code changes, you MUST follow this process:

### 1. Code Formatting & Linting
Run the Gradle command to format code and fix linting issues:

```bash
./gradlew :detektFormat
```

### 2. Run Tests
Run relevant tests to ensure functionality is intact. Examples for different modules:

```bash
# Twitter module end-to-end tests
./gradlew :twitter:e2e:test

# Translation module tests
./gradlew :translations:google:test
./gradlew :translations:e2e:test

# Video module tests
./gradlew :video:e2e:test

# Article module tests
./gradlew :article:e2e:test

# AI module tests
./gradlew :ai:e2e:test

# Localization module tests
./gradlew :localization:impl:test

# Application tests
./gradlew :app:test
```

### 3. Documentation & Compilation
Ensure all documentation in */CLAUDE.md is updated if necessary and fix any compilation errors before proceeding.

## Tools
Use MCP context7 for finding latest library versions:
```
/mcp__context7__resolveLibraryId ${libraryName}
```

For example, to find the latest version of `ktor-client-core`, use:
```
/mcp__context7__resolveLibraryId ktor-client-core
```

## Development Memories
- Add env variables to docker compose files
