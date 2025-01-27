package com.programistich.twitterx.core.telegram.models

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import org.springframework.context.annotation.Bean
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient
import org.telegram.telegrambots.meta.generics.TelegramClient

@ConfigurationProperties("telegram", ignoreInvalidFields = false)
data class TelegramConfig @ConstructorBinding constructor(
    val botToken: String,
    val botUsername: String,
    val ownerId: String
) {
    @Bean
    fun getTelegramClient(telegramConfig: TelegramConfig): TelegramClient {
        return OkHttpTelegramClient(telegramConfig.botToken)
    }
}
