package com.programistich.twitterx.core.openai

import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import kotlin.time.Duration.Companion.seconds

@Configuration
open class OpenAIConfiguration {
    @Bean
    open fun getOpenAIApi(@Value("\${open-ai.api-key}") apiKey: String): OpenAI {
        return OpenAI(
            token = apiKey,
            timeout = Timeout(socket = 60.seconds),
            logging = LoggingConfig(logLevel = LogLevel.None)
        )
    }
}
