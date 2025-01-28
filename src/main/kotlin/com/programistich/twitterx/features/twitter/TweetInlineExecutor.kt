package com.programistich.twitterx.features.twitter

import com.programistich.twitterx.core.executors.InlineQueryExecutor
import com.programistich.twitterx.core.telegram.models.Language
import com.programistich.twitterx.core.telegram.models.TelegramContext
import com.programistich.twitterx.core.telegram.updates.TelegramInlineQuery
import com.programistich.twitterx.core.telegraph.TelegraphApi
import com.programistich.twitterx.core.twitter.Tweet
import com.programistich.twitterx.core.twitter.TweetContent
import com.programistich.twitterx.core.twitter.TwitterApi
import com.programistich.twitterx.features.dict.DictionaryCache
import com.programistich.twitterx.features.dict.DictionaryKey
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultPhoto
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow
import org.telegram.telegrambots.meta.generics.TelegramClient
import java.util.UUID

@Component
class TweetInlineExecutor(
    private val telegramClient: TelegramClient,
    private val twitterApi: TwitterApi,
    private val telegraphApi: TelegraphApi,
    private val dictionary: DictionaryCache
) : InlineQueryExecutor() {
    override suspend fun canProcess(context: TelegramContext<TelegramInlineQuery>): Boolean {
        val text = context.update.getQuery()
        return twitterApi.getTweetIds(text).isNotEmpty()
    }

    override suspend fun process(context: TelegramContext<TelegramInlineQuery>): Result<Unit> {
        val text = context.update.getQuery()
        val tweetId = twitterApi.getTweetIds(text).firstOrNull() ?: return Result.failure(Exception("Tweet id is null"))

        val results = getTweetsInParallel(tweetId)
            .map { it.first.getOrThrow() to it.second }
            .map { toInlineQueryResult(it.first, it.second) }

        val answer = AnswerInlineQuery(context.update.getId(), results)
        telegramClient.execute(answer)

        return Result.success(Unit)
    }

    private suspend fun toInlineQueryResult(tweet: Tweet, language: Language): InlineQueryResult {
        val title = dictionary.getByKey(DictionaryKey.TWEET_INLINE_TITLE, language, tweet.author.username)

        val text = getText(tweet)
        val content = InputTextMessageContent(text)
        val url = tweet.url
        val id = UUID.randomUUID().toString()

        return when (tweet.content) {
            is TweetContent.ManyMedia -> {
                val photo = tweet.content.mosaic ?: tweet.content.urls.first()
                val inlineQueryResult = InlineQueryResultPhoto(id, photo)
                inlineQueryResult.title = title
                inlineQueryResult.description = title
                inlineQueryResult.caption = text
                inlineQueryResult.thumbnailUrl = photo
                inlineQueryResult.photoUrl = photo
                inlineQueryResult.showCaptionAboveMedia = true
                inlineQueryResult
            }
            is TweetContent.Photo -> {
                val photo = tweet.content.url
                val inlineQueryResult = InlineQueryResultPhoto(id, photo)
                inlineQueryResult.title = title
                inlineQueryResult.description = title
                inlineQueryResult.caption = text
                inlineQueryResult.thumbnailUrl = photo
                inlineQueryResult.photoUrl = photo
                inlineQueryResult.showCaptionAboveMedia = true
                inlineQueryResult
            }
            is TweetContent.Poll -> {
                val inlineQueryResult = InlineQueryResultArticle(id, title, InputTextMessageContent(text))
                inlineQueryResult.replyMarkup = tweet
                    .content.options.map { InlineKeyboardRow(it) }.let { InlineKeyboardMarkup(it) }
                inlineQueryResult.url = url
                inlineQueryResult.title = title
                inlineQueryResult
            }
            TweetContent.Text -> {
                val inlineQueryResult = InlineQueryResultArticle(id, title, content)
                inlineQueryResult.url = url
                inlineQueryResult
            }
            is TweetContent.Video -> {
                val photo = tweet.content.thumbnailUrl
                val inlineQueryResult = InlineQueryResultPhoto(id, photo)
                inlineQueryResult.title = title
                inlineQueryResult.caption = text
                inlineQueryResult.thumbnailUrl = photo
                inlineQueryResult.photoUrl = photo
                inlineQueryResult
            }
        }
    }

    private suspend fun getText(tweet: Tweet): String {
        val content = tweet.getContent()
        val limit = tweet.content.getLimit()

        return if (content.length > limit) {
            val url = telegraphApi.createPage(
                title = "\u200E",
                content = content
            ).getOrThrow()
            url
        } else {
            content
        }
    }

    private suspend fun getTweetsInParallel(tweetId: String): List<Pair<Result<Tweet>, Language>> = coroutineScope {
        Language.entries.map { language ->
            async {
                twitterApi.getTweet(tweetId, language.iso) to language
            }
        }.awaitAll()
    }
}
