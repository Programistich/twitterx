package com.programistich.twitterx

import com.programistich.twitterx.core.telegram.models.TelegramConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(TelegramConfig::class)
open class TwitterXApplication

fun main() {
    runApplication<TwitterXApplication>()
}
