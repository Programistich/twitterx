# AI Module

This module provides AI text generation capabilities for the TwitterX project using Google Gemini Web API through Python subprocess integration.

## Module Structure

The AI module is organized into several sub-modules:

- **ai:api** - Core interfaces and models for AI functionality
- **ai:google** - Google Gemini Web API implementation using Python subprocess
- **ai:e2e** - End-to-end tests for AI functionality

## Architecture

The AI module follows a hybrid Kotlin-Python architecture:

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Kotlin Code   │───▶│  Python Process  │───▶│ Gemini Web API  │
│   (GoogleAI     │    │  (gemini_ai.py)  │    │ (gemini.google. │
│    Service)     │    │                  │    │  com)           │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

This approach provides:
- **Type-safe Kotlin API** for integration with the rest of the application
- **Python subprocess execution** for leveraging the robust `gemini_webapi` library
- **Clean separation** between Kotlin business logic and Python AI integration

## AI API (ai:api)

Core interfaces and models for AI functionality.

### Key Components

#### AIService Interface
Main service contract for AI operations:

```kotlin
interface AIService {
    suspend fun generateContent(request: AIRequest): Result<AIResponse>
    suspend fun startChat(model: AIModel = AIModel.UNSPECIFIED): Result<ChatSession>
    suspend fun sendMessage(session: ChatSession, prompt: String, files: List<String> = emptyList()): Result<AIResponse>
    suspend fun isAvailable(): Boolean
    suspend fun close()
}
```

#### Data Models
- `AIRequest` - Request for text generation with prompt, model, and optional files
- `AIResponse` - Response containing generated text, thoughts, images, and metadata
- `AIModel` - Enumeration of supported Gemini models (2.5 Flash, 2.5 Pro, etc.)
- `ChatSession` - Session metadata for multi-turn conversations
- `AIImage` - Image data from AI responses (web images or generated images)

#### Exception Hierarchy
Comprehensive error handling for AI operations:
- `AIException` - Base exception class
- `AIAuthException` - Authentication errors
- `AIApiException` - API-related errors
- `AITimeoutException` - Request timeout errors
- `AIUsageLimitException` - Model usage limit exceeded
- `AIModelInvalidException` - Invalid model specified
- `AITemporarilyBlockedException` - IP temporarily blocked
- `AIProcessException` - Python subprocess execution errors

## Google Implementation (ai:google)

Google Gemini Web API implementation using Python subprocess integration.

### Key Components

#### GoogleAIService
Main implementation of `AIService` that manages Python subprocess execution:

```kotlin
class GoogleAIService(
    private val configuration: GoogleAIConfiguration = GoogleAIConfiguration.DEFAULT
) : AIService
```

#### PythonProcessExecutor
Handles execution of Python Gemini scripts:
- **Command serialization** - Converts Kotlin commands to JSON for Python
- **Process management** - Manages subprocess lifecycle and timeouts
- **Response parsing** - Parses JSON responses from Python
- **Error mapping** - Maps Python exceptions to Kotlin exception hierarchy

#### GoogleAIConfiguration
Configuration for Google AI service:

```kotlin
data class GoogleAIConfiguration(
    val secure1PSID: String? = null,           // Google authentication cookie
    val secure1PSIDTS: String? = null,         // Google authentication cookie
    val proxy: String? = null,                 // Optional proxy URL
    val timeout: Long = 30_000L,               // Request timeout in milliseconds
    val autoClose: Boolean = false,            // Auto-close client after inactivity
    val closeDelay: Long = 300_000L,           // Delay before auto-close
    val autoRefresh: Boolean = true,           // Auto-refresh authentication
    val refreshInterval: Long = 540_000L,      // Refresh interval in milliseconds
    val pythonExecutable: String = "python3",  // Python executable path
    val scriptPath: String = "scripts/gemini_ai.py" // Python script path
)
```

### Python Integration

#### gemini_ai.py Script
Python CLI wrapper for `gemini_webapi` library located in `scripts/`:

**Supported Actions:**
- `generate` - Single-turn text generation
- `start_chat` - Initialize chat session
- `send_message` - Send message in existing chat
- `check_availability` - Test service availability

**Authentication Methods:**
- **Browser cookies** - Automatic extraction from local browser
- **Manual cookies** - Explicit `__Secure-1PSID` and `__Secure-1PSIDTS`
- **Environment variables** - `GEMINI_SECURE_1PSID`, `GEMINI_SECURE_1PSIDTS`

#### Communication Protocol
JSON-based communication between Kotlin and Python:

**Request Format:**
```json
{
  "action": "generate",
  "prompt": "Hello, world!",
  "model": "gemini-2.5-flash",
  "files": [],
  "conversationId": null,
  "metadata": null,
  "secure1PSID": "cookie_value",
  "secure1PSIDTS": "cookie_value",
  "timeout": 30000
}
```

**Response Format:**
```json
{
  "success": true,
  "text": "Hello! How can I help you today?",
  "thoughts": null,
  "conversationId": "chat_123_456",
  "metadata": ["chat_id", "reply_id", "candidate_id"],
  "model": "gemini-2.5-flash",
  "images": []
}
```

### Supported Models

Based on Google Gemini Web API:
- `unspecified` - Default model
- `gemini-2.5-flash` - Gemini 2.5 Flash (fast, efficient)
- `gemini-2.5-pro` - Gemini 2.5 Pro (advanced capabilities, daily limits)
- `gemini-2.0-flash` - Gemini 2.0 Flash (deprecated)
- `gemini-2.0-flash-thinking` - Gemini 2.0 Flash with thinking (deprecated)

### Features

#### Text Generation
- **Single-turn generation** - Simple prompt-response interaction
- **Multi-turn conversations** - Persistent chat sessions with metadata persistence
- **File attachments** - Support for images and documents
- **Model selection** - Choose specific Gemini models
- **Thoughts access** - Access to model's reasoning process (thinking models)
- **Stateless chat sessions** - Chat metadata is passed between Kotlin and Python for session continuity

#### Image Support
- **Web images** - Images retrieved from web searches
- **Generated images** - AI-generated images via Imagen4
- **Image metadata** - URLs, titles, descriptions, and generation flags

#### Error Handling
- **Authentication errors** - Invalid or expired cookies
- **Rate limiting** - Usage limits and temporary blocks
- **Timeout handling** - Request timeout management
- **Model availability** - Model-specific error handling

## End-to-End Testing (ai:e2e)

Comprehensive test suite for AI functionality validation.

### Test Requirements

**Prerequisites:**
1. Python 3 with `gemini_webapi` installed (`pip install gemini_webapi`)
2. Valid Google Gemini authentication cookies (optional for browser mode)
3. Internet connection for API access

**Environment Variables:**
- `GEMINI_SECURE_1PSID` - Primary authentication cookie
- `GEMINI_SECURE_1PSIDTS` - Secondary authentication cookie (optional)

### Test Cases

The E2E module covers:

- **Service availability** - Basic connectivity and Python script execution
- **Text generation** - Single-turn content generation with different models
- **Chat functionality** - Multi-turn conversation sessions
- **Model testing** - Validation across different Gemini models
- **Error scenarios** - Authentication failures and timeout handling

### Running Tests

```bash
# Install Python dependencies
pip install -r scripts/requirements.txt

# Set authentication (optional - will use browser cookies if available)
export GEMINI_SECURE_1PSID="your_cookie_here"
export GEMINI_SECURE_1PSIDTS="your_cookie_here"

# Run E2E tests
./gradlew :ai:e2e:test
```

## Integration with TwitterX

The AI module integrates with the main TwitterX application to:

- **Support `/gpt` command** - Enable AI chat functionality in Telegram bot
- **Generate responses** - Create AI-powered responses to user queries
- **Multi-language support** - Work with TwitterX's localization system
- **Error handling** - Provide meaningful error messages in user's language

## Dependencies

### Kotlin Dependencies
- `kotlinx-coroutines` - Asynchronous operations
- `kotlinx-serialization` - JSON serialization for Python communication
- `slf4j` - Logging framework

### Python Dependencies
- `gemini_webapi` - Google Gemini Web API client
- `asyncio` - Asynchronous operations in Python
- `json` - JSON serialization/deserialization

## Configuration

### Authentication Setup

1. **Browser Cookie Mode (Recommended):**
   - Login to https://gemini.google.com in your browser
   - The Python script will automatically extract cookies

2. **Manual Cookie Mode:**
   - Go to https://gemini.google.com and login
   - Open browser DevTools (F12) → Network tab
   - Refresh page and find any request
   - Copy `__Secure-1PSID` and `__Secure-1PSIDTS` cookie values
   - Set as environment variables or pass to configuration

3. **Environment Variables:**
   ```bash
   export GEMINI_SECURE_1PSID="your_secure_1psid_value"
   export GEMINI_SECURE_1PSIDTS="your_secure_1psidts_value"
   ```

### Python Environment Setup

```bash
# Navigate to project root
cd /path/to/twitterx

# Install Python dependencies
pip install -r scripts/requirements.txt

# Test Python script directly
python3 scripts/gemini_ai.py '{"action": "check_availability"}'
```

## Testing

### Unit Tests
```bash
./gradlew :ai:api:test
./gradlew :ai:google:test
```

### End-to-End Tests
```bash
./gradlew :ai:e2e:test
```

## Troubleshooting

### Common Issues

1. **Python Import Errors:**
   - Ensure `gemini_webapi` is installed: `pip install gemini_webapi`
   - Check Python path in configuration

2. **Authentication Failures:**
   - Verify cookies are current and valid
   - Try browser cookie mode instead of manual cookies
   - Check for IP-based restrictions

3. **Timeout Errors:**
   - Increase timeout in configuration
   - Check internet connectivity
   - Verify Google Gemini service availability

4. **Process Execution Errors:**
   - Ensure Python 3 is available and in PATH
   - Check script permissions: `chmod +x scripts/gemini_ai.py`
   - Verify working directory is project root

### Debugging

Enable debug logging to troubleshoot issues:

```kotlin
val config = GoogleAIConfiguration(
    timeout = 60_000L, // Increase timeout for debugging
    // ... other settings
)
```

Check Python script output directly:
```bash
python3 scripts/gemini_ai.py '{"action": "check_availability", "secure1PSID": "your_cookie"}'
```

# AI Module Setup Guide

## Quick Start

1. **Install Python dependencies:**
   ```bash
   pip install -r scripts/requirements.txt
   ```

2. **Test the setup:**
   ```bash
   python3 scripts/gemini_ai.py '{"action": "check_availability"}'
   ```

3. **Set up authentication (optional):**
   ```bash
   export GEMINI_SECURE_1PSID="your_cookie_value"
   export GEMINI_SECURE_1PSIDTS="your_cookie_value"  # optional
   ```

4. **Run tests:**
   ```bash
   ./gradlew :ai:api:test :ai:google:test
   ```

## Authentication Setup

### Option 1: Browser Cookies (Recommended)
1. Open https://gemini.google.com in your browser
2. Login with your Google account
3. The Python script will automatically extract cookies

### Option 2: Manual Cookies
1. Go to https://gemini.google.com and login
2. Press F12 → Network tab → Refresh page
3. Click any request and copy cookie values:
  - `__Secure-1PSID`
  - `__Secure-1PSIDTS` (optional)
4. Set environment variables:
   ```bash
   export GEMINI_SECURE_1PSID="your_value_here"
   export GEMINI_SECURE_1PSIDTS="your_value_here"
   ```

## Testing

### Basic Test
```bash
# Test Python script directly
python3 scripts/gemini_ai.py '{"action": "check_availability"}'
```

### Unit Tests
```bash
./gradlew :ai:api:test :ai:google:test
```

### E2E Tests (requires authentication)
```bash
./gradlew :ai:e2e:test
```

## Integration with TwitterX

The AI module is already integrated into the main application and will be available for the `/gpt` command.

### Example Usage in Code
```kotlin
val aiService = GoogleAIService()
val request = AIRequest(
    prompt = "Hello, how are you?",
    model = AIModel.GEMINI_2_5_FLASH
)

val result = aiService.generateContent(request)
result.fold(
    onSuccess = { response ->
        println("AI Response: ${response.text}")
    },
    onFailure = { error ->
        println("Error: ${error.message}")
    }
)
```

## Troubleshooting

### Python Import Errors
```bash
# Install gemini_webapi
pip install gemini_webapi

# Or install all dependencies
pip install -r scripts/requirements.txt
```

### Authentication Issues
- Make sure cookies are fresh (login again)
- Try browser cookie mode instead of manual cookies
- Check for IP restrictions

### Process Execution Errors
- Ensure Python 3 is in PATH
- Verify working directory is project root
- Check script permissions: `chmod +x scripts/gemini_ai.py`