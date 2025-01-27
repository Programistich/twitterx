package com.programistich.twitterx.core.telegram.models

import com.programistich.twitterx.core.repos.TelegramChat
import com.programistich.twitterx.core.telegram.updates.TelegramUpdate

data class TelegramContext<UPDATE : TelegramUpdate>(
    val update: UPDATE,
    val config: TelegramConfig,
    val chat: TelegramChat? = null,
)
