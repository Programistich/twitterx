package com.programistich.twitterx.telegram.models

import com.programistich.twitterx.entities.TelegramChat

data class TelegramContext(
    val update: TelegramUpdate,
    val config: TelegramConfig,
    val command: TelegramCommand?,
    val chat: TelegramChat?
)
