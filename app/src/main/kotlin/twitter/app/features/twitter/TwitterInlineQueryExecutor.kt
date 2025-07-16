package twitter.app.features.twitter

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import twitterx.article.api.ArticleService
import twitterx.localization.api.LocalizationService
import twitterx.localization.api.MessageKey
import twitterx.telegram.api.TelegramClient
import twitterx.telegram.api.executors.InlineQueryExecutor
import twitterx.telegram.api.models.TelegramContext
import twitterx.telegram.api.models.inline.InlineQueryResult
import twitterx.telegram.api.models.inline.InlineQueryResultArticle
import twitterx.telegram.api.models.inline.InlineQueryResultPhoto
import twitterx.telegram.api.models.inline.InputTextMessageContent
import twitterx.telegram.api.updates.TelegramInlineQuery
import twitterx.translation.api.Language
import twitterx.translation.api.TranslationService
import twitterx.twitter.api.Tweet
import twitterx.twitter.api.TweetsThread
import twitterx.twitter.api.TwitterService

@Component
public class TwitterInlineQueryExecutor(
    private val twitterService: TwitterService,
    private val telegramClient: TelegramClient,
    private val translationService: TranslationService,
    private val localizationService: LocalizationService,
    private val articleService: ArticleService
) : InlineQueryExecutor() {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private companion object {
        private const val TELEGRAM_MESSAGE_MAX_LENGTH = 4096
        private const val TELEGRAM_CAPTION_MAX_LENGTH = 1024
    }

    override suspend fun canProcess(context: TelegramContext<TelegramInlineQuery>): Boolean {
        val query = context.update.query.trim()
        return query.isNotEmpty() && twitterService.getTweetId(query).isSuccess
    }

    override suspend fun process(context: TelegramContext<TelegramInlineQuery>) {
        val query = context.update.query.trim()
        val inlineQueryId = context.update.queryId

        logger.info("Processing inline query: $query")

        val results = try {
            processTwitterQuery(query)
        } catch (e: Exception) {
            logger.error("Failed to process inline query: $query", e)
            emptyList()
        }

        val result = telegramClient.answerInlineQuery(
            inlineQueryId = inlineQueryId,
            results = results,
            cacheTime = 300,
            isPersonal = true
        )

        if (result.isFailure) {
            logger.error("Failed to answer inline query $inlineQueryId", result.exceptionOrNull())
        } else {
            logger.info("Successfully answered inline query $inlineQueryId with ${results.size} results")
        }
    }

    private suspend fun processTwitterQuery(query: String): List<InlineQueryResult> {
        val tweetIdResult = twitterService.getTweetId(query)
        if (tweetIdResult.isFailure) {
            logger.error("Failed to extract tweet ID from: $query")
            return emptyList()
        }

        val tweetId = tweetIdResult.getOrThrow()
        val usernameResult = twitterService.getUsername(query)
        if (usernameResult.isFailure) {
            logger.error("Failed to extract username from: $query")
            return emptyList()
        }

        val username = usernameResult.getOrThrow()
        val tweetThreadResult = twitterService.getTweetThread(username, tweetId)

        if (tweetThreadResult.isFailure) {
            logger.error("Failed to fetch tweet thread for $username/$tweetId", tweetThreadResult.exceptionOrNull())
            return emptyList()
        }

        val tweetThread = tweetThreadResult.getOrThrow()
        return createInlineResults(tweetThread)
    }

    private suspend fun createInlineResults(tweetThread: TweetsThread): List<InlineQueryResult> {
        val results = mutableListOf<InlineQueryResult>()
        val mainTweet = getMainTweet(tweetThread)

        // Create results for all three supported languages
        for (language in Language.values()) {
            val languageEmoji = when (language) {
                Language.ENGLISH -> "🇬🇧"
                Language.UKRAINIAN -> "🇺🇦"
                Language.RUSSIAN -> "🇷🇺"
            }

            val result = createInlineResultForLanguage(mainTweet, language, languageEmoji)
            if (result != null) {
                results.add(result)
            }
        }

        return results
    }

    private suspend fun createInlineResultForLanguage(
        tweet: Tweet,
        language: Language,
        languageEmoji: String
    ): InlineQueryResult? {
        val translatedContent = translateTweetContent(tweet.content, language)
        val header = formatTweetHeader(tweet, language)
        val fullContent = "$header\n\n$translatedContent"

        val mediaResult = InlineMediaProcessor.processMediaForInline(tweet)
        val resultId = "${tweet.id}_${language.iso}"
        val title = "$languageEmoji ${tweet.username} - ${language.name}"
        val description = tweet.content.take(100) + if (tweet.content.length > 100) "..." else ""

        return if (mediaResult.hasMedia && mediaResult.photoUrl != null) {
            // Create photo result
            val caption = if (fullContent.length > TELEGRAM_CAPTION_MAX_LENGTH) {
                val articleResult = articleService.createArticle(translatedContent, "Tweet")
                if (articleResult.isSuccess) {
                    val articleUrl = articleResult.getOrThrow()
                    "$header\n\n$articleUrl"
                } else {
                    fullContent.take(TELEGRAM_CAPTION_MAX_LENGTH - 3) + "..."
                }
            } else {
                fullContent
            }

            InlineQueryResultPhoto(
                id = resultId,
                photoUrl = mediaResult.photoUrl,
                thumbUrl = mediaResult.photoUrl,
                title = title,
                description = description,
                caption = caption,
                parseMode = "HTML"
            )
        } else {
            // Create article result
            val messageText = if (fullContent.length > TELEGRAM_MESSAGE_MAX_LENGTH) {
                val articleResult = articleService.createArticle(translatedContent, "Tweet")
                if (articleResult.isSuccess) {
                    val articleUrl = articleResult.getOrThrow()
                    "$header\n\n$articleUrl"
                } else {
                    fullContent.take(TELEGRAM_MESSAGE_MAX_LENGTH - 3) + "..."
                }
            } else {
                fullContent
            }

            InlineQueryResultArticle(
                id = resultId,
                title = title,
                description = description,
                inputMessageContent = InputTextMessageContent(
                    messageText = messageText,
                    parseMode = "HTML",
                    disableWebPagePreview = true
                )
            )
        }
    }

    private fun getMainTweet(tweetThread: TweetsThread): Tweet {
        return when (tweetThread) {
            is TweetsThread.Single -> tweetThread.tweet
            is TweetsThread.Reply -> tweetThread.tweet
            is TweetsThread.QuoteThread -> tweetThread.tweet
            is TweetsThread.RetweetThread -> tweetThread.tweet
        }
    }

    private suspend fun translateTweetContent(content: String, userLanguage: Language): String {
        if (content.isEmpty()) return ""

        val translationResult = translationService.translate(content, userLanguage)

        return if (translationResult.isSuccess) {
            val translation = translationResult.getOrThrow()
            if (translation.isSameLanguage()) {
                content
            } else {
                "[${translation.to.iso.uppercase()}] ${translation.text}\n\n[${translation.from.uppercase()}] $content"
            }
        } else {
            logger.warn("Failed to translate tweet content", translationResult.exceptionOrNull())
            content
        }
    }

    private suspend fun formatTweetHeader(tweet: Tweet, userLanguage: Language): String {
        val parameters = mapOf(
            "username" to tweet.username.removePrefix("@"),
            "fullName" to tweet.fullName,
            "url" to tweet.tweetUrl,
            "userUrl" to tweet.account.url
        )
        return localizationService.getMessage(MessageKey.TWEET_FROM, userLanguage, parameters)
    }
}
