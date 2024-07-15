package com.programistich.twitterx

import com.programistich.twitterx.telegram.models.TelegramConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(TelegramConfig::class)
open class TwitterXApplication

fun main(args: Array<String>) {
    runApplication<TwitterXApplication>(*args)
}
