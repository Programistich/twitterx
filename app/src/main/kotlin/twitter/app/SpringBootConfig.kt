package twitter.app

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import translations.libre.LibreTranslateService
import twitter.app.telegram.TelegramClientImpl
import twitterx.article.api.ArticleService
import twitterx.article.telegraph.TelegraphService
import twitterx.localization.api.LocalizationService
import twitterx.localization.impl.FileBasedLocalizationService
import twitterx.telegram.api.TelegramClient
import twitterx.telegram.api.TelegramService
import twitterx.telegram.api.executors.Executor
import twitterx.telegram.api.models.TelegramConfig
import twitterx.telegram.api.updates.TelegramUpdate
import twitterx.translation.api.TranslationService
import twitterx.twitter.api.TwitterService
import twitterx.twitter.fx.FxTwitterService
import twitterx.twitter.impl.TwitterServiceImpl
import twitterx.twitter.nitter.NitterService
import twitterx.video.ytdlp.YtDlpConfig
import twitterx.video.ytdlp.YtDlpVideoService

@Component
public object SpringBootConfig {

    @Bean
    public fun telegramConfig(
        @Value("\${telegram.bot.token}") botToken: String,
        @Value("\${telegram.bot.username}") botUsername: String,
        @Value("\${telegram.owner.id}") ownerId: String,
    ): TelegramConfig {
        return TelegramConfig(
            botToken = botToken,
            botUsername = botUsername,
            ownerId = ownerId,
        )
    }

    @Bean
    public fun telegramService(
        executors: MutableList<Executor<out TelegramUpdate>>,
        telegramConfig: TelegramConfig
    ): TelegramService {
        return TelegramService(
            executors = executors,
            config = telegramConfig
        )
    }

    @Bean
    public fun localizationService(): LocalizationService {
        return FileBasedLocalizationService()
    }

    @Bean
    public fun httpClient(): HttpClient {
        return HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    }
                )
            }
            install(Logging) {
                level = LogLevel.BODY
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 60_000 // 60 seconds
                connectTimeoutMillis = 10_000 // 10 seconds
                socketTimeoutMillis = 60_000 // 60 seconds
            }
        }
    }

    @Bean
    public fun twitterService(
        @Value("\${nitter.base.url}") nitterBaseUrl: String,
        @Value("\${fx.base.url}") fxBaseUrl: String,
        httpClient: HttpClient
    ): TwitterService {
        val nitterService = NitterService(
            httpClient = httpClient,
            nitterBaseUrl = nitterBaseUrl
        )

        val fxService = FxTwitterService(
            httpClient = httpClient,
            baseUrl = fxBaseUrl
        )

        return TwitterServiceImpl(
            idProvider = nitterService,
            tweetProvider = fxService,
            accountProvider = nitterService
        )
    }

    @Bean
    public fun translationService(
        httpClient: HttpClient,
        @Value("\${open.router.api.key}") apiKey: String,
    ): TranslationService {
        // return GoogleTranslationService(httpClient)
//        return QwenTranslationService(httpClient, apiKey)
        return LibreTranslateService(httpClient)
    }

    @Bean
    public fun articleService(
        httpClient: HttpClient
    ): ArticleService {
        return TelegraphService(httpClient)
    }

    @Bean
    public fun telegramClient(
        telegramConfig: TelegramConfig
    ): TelegramClient {
        return TelegramClientImpl(
            botToken = telegramConfig.botToken
        )
    }

    @Bean
    public fun videoService(
        @Value("\${video.yt-dlp.executable-path:yt-dlp}") executablePath: String,
        @Value("\${video.yt-dlp.cookies-file:cookies.txt}") cookiesFile: String
    ): twitterx.video.api.VideoService {
        val config = YtDlpConfig(
            executablePath = executablePath,
            cookiesFile = cookiesFile
        )
        return YtDlpVideoService(config)
    }

//    @Bean
//    public fun aiService(
//        @Value("\${secure1PSID}") secure1PSID: String,
//        @Value("\${secure1PSIDTS}") secure1PSIDTS: String,
//    ): AIService {
//        val googleAIConfiguration = GoogleAIConfiguration(
//            secure1PSID = secure1PSID,
//            secure1PSIDTS = secure1PSIDTS,
//            scriptPath = "scripts/gemini_ai.py"
//        )
//
//        return GoogleAIService(googleAIConfiguration)
//    }
}
