package twitterx.telegram.api.models

import twitterx.telegram.api.updates.TelegramUpdate

public data class TelegramContext<UPDATE : TelegramUpdate>(
    public val update: UPDATE,
    public val config: TelegramConfig,
)
