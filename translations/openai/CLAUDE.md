# OpenAI Translation Module

This module provides text translation capabilities using an OpenAI-compatible API, specifically targeting the OpenRouter gateway. It is designed to leverage structured JSON outputs from models like GPT for reliable translation and source language detection.

## Key Components

### OpenAITranslationService
The main service implementation that conforms to the `TranslationService` interface.

- **API Gateway**: Connects to `https://openrouter.ai/api/v1`.
- **Model**: Defaults to `gpt-5-mini` but can be configured.
- **Structured Output**: Uses the `/responses` API with `text.format: { "type": "json_object" }` to enforce predictable JSON output.
- **Prompt Engineering**: A detailed prompt instructs the model to perform translation, detect the source language, and return the result in a specific JSON format.
- **Error Handling**: Includes robust error handling for API failures and response parsing issues.

### OpenAIModels
Internal data classes for serializing requests to and deserializing responses from the OpenAI-compatible API.

- `ResponsesRequest`: Contains `model`, `input`, and `text.format`.
- `ResponsesResponse`: Contains `output` array with `message` item and `output_text` content.
- `TranslationResult`: A specific data class to deserialize the JSON content from the model's message, containing the translated `text` and the `sourceLanguage`.

## Prompt Strategy

The service uses a carefully crafted prompt to guide the AI model:

You are a translation assistant. Your task is to translate the given text and identify its source language.
You MUST return a JSON object with the following structure:
{
"text": "The translated text goes here.",
"sourceLanguage": "The detected ISO 639-1 code of the source language (e.g., 'en', 'uk', 'ru')."
}

Translate the following text to {Target Language}.

Detect the source language of the text.

If the source language is the same as the target language, return the original text.

If the source language cannot be determined, use 'en' as the default.

Text to translate:
"{text}"

This approach ensures high reliability in parsing the model's output, which is a common challenge with LLMs.

## Configuration

The service requires an API key for OpenRouter, which is provided via the `open.router.api.key` property in the application configuration.

## Integration

This service is integrated into the main application via the `SpringBootConfig` as the primary `TranslationService` bean. It replaces other translation implementations like Google, Qwen, or LibreTranslate.
