package com.programistich.twitterx.features.twitter

import com.programistich.twitterx.core.executors.Executor
import com.programistich.twitterx.core.repos.TelegramChat
import com.programistich.twitterx.core.telegram.models.Language
import com.programistich.twitterx.core.telegram.models.TelegramConfig
import com.programistich.twitterx.core.telegram.models.TelegramContext
import com.programistich.twitterx.core.telegram.updates.TelegramMessageUpdate
import com.programistich.twitterx.features.dict.DictionaryCache
import com.programistich.twitterx.features.dict.DictionaryKey
import com.programistich.twitterx.features.telegraph.TelegraphApi
import com.programistich.twitterx.features.twitter.TweetContent.ManyMedia
import com.programistich.twitterx.features.twitter.TweetContent.Photo
import com.programistich.twitterx.features.twitter.TweetContent.Poll
import com.programistich.twitterx.features.twitter.TweetContent.Text
import com.programistich.twitterx.features.twitter.TweetContent.Video
import kotlinx.coroutines.future.await
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.methods.send.SendVideo
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto
import org.telegram.telegrambots.meta.api.objects.polls.input.InputPollOption
import org.telegram.telegrambots.meta.generics.TelegramClient

@Component
class TweetExecutor(
    private val twitterApi: TwitterApi,
    private val telegraphApi: TelegraphApi,
    private val dictionary: DictionaryCache,
    private val telegramConfig: TelegramConfig,
    private val telegramClient: TelegramClient
) : Executor<TelegramMessageUpdate> {
    companion object {
        private const val TWEET_REGEX = "https://(?:mobile.)?(?:twitter.com|x.com)/([a-zA-Z0-9_]+)/status/([0-9]+)?(.*)"
    }

    private val logger = LoggerFactory.getLogger(this::class.java)

    override val priority: Executor.Priority
        get() = Executor.Priority.MEDIUM

    private fun getTweetIds(text: String): List<String> {
        return TWEET_REGEX.toRegex().findAll(text).map { it.groupValues[2] }.toList()
    }

    override suspend fun canProcess(context: TelegramContext<TelegramMessageUpdate>): Boolean {
        val text = context.update.getText() ?: return false
        return getTweetIds(text).isNotEmpty()
    }

    override suspend fun process(context: TelegramContext<TelegramMessageUpdate>): Result<Unit> {
        val from = context.update.getFrom() ?: return Result.failure(Exception("From is null"))
        val chat = context.chat ?: return Result.failure(Exception("Chat is null"))
        val text = context.update.getText() ?: return Result.failure(Exception("Text is null"))
        val tweetId = getTweetIds(text).firstOrNull() ?: return Result.failure(Exception("Tweet id is null"))

        val tweet = twitterApi
            .getTweet(tweetId, chat.language.iso)
            .getOrNull()
            ?: return handleTweetException(tweetId = tweetId, to = chat)

        runCatching {
            val deleteMessage = DeleteMessage(chat.idStr(), context.update.messageId())
            telegramClient.executeAsync(deleteMessage).await()
        }

        return handleTweet(tweet = tweet, to = chat, from = from)
    }

    private suspend fun handleTweetException(tweetId: String, to: TelegramChat): Result<Unit> {
        logger.error("Tweet not found: $tweetId")
        val fixUrl = "https://fixupx.com/status/$tweetId/${to.language.iso}"
        val text = dictionary.getByKey(DictionaryKey.TWEET_FIX_UPX, to.language, fixUrl)

        runCatching {
            val sendMessage = SendMessage(telegramConfig.ownerId, "Tweet not found: $tweetId")
            telegramClient.executeAsync(sendMessage).await()
        }

        return runCatching {
            val sendMessage = SendMessage(to.idStr(), text)
            sendMessage.disableWebPagePreview = false
            telegramClient.executeAsync(sendMessage).await()
        }
    }

    private suspend fun handleTweet(
        tweet: Tweet,
        to: TelegramChat,
        from: User
    ): Result<Unit> {
        val header = getHeaderText(to.language, tweet, from) + "\n\n"
        val content = tweet.getContent()

        val limit = getLimit(tweet.content)
        val text = if (header.length + content.length > limit) {
            val url = telegraphApi.createPage(
                title = "\u200E",
                content = content
            ).getOrThrow()
            "$header$url"
        } else {
            "$header$content"
        }

        return sendTweet(tweet, text, to)
    }

    private suspend fun sendTweet(
        tweet: Tweet,
        text: String,
        to: TelegramChat,
    ): Result<Unit> = runCatching {
        when (tweet.content) {
            is ManyMedia -> {
                val action = SendChatAction(to.idStr(), "upload_photo")
                telegramClient.executeAsync(action).await()

                val medias = tweet.content.urls.mapIndexed { index, url ->
                    val media = InputMediaPhoto(url)
                    if (index == 0) {
                        media.caption = text
                        media.parseMode = "HTML"
                    }
                    media
                }
                val method = SendMediaGroup(to.idStr(), medias)
                telegramClient.executeAsync(method).await()
            }
            is Photo -> {
                val action = SendChatAction(to.idStr(), "upload_photo")
                telegramClient.executeAsync(action).await()

                val inputFile = InputFile(tweet.content.url)
                val sendPhoto = SendPhoto(to.idStr(), inputFile)
                sendPhoto.caption = text
                sendPhoto.parseMode = "HTML"
                telegramClient.executeAsync(sendPhoto).await()
            }
            is Poll -> {
                val action = SendChatAction(to.idStr(), "typing")
                telegramClient.executeAsync(action).await()

                val options = tweet.content.options.map { InputPollOption(it) }
                val sendPoll = SendPoll(to.idStr(), text, options)
                telegramClient.executeAsync(sendPoll).await()
            }
            Text -> {
                val action = SendChatAction(to.idStr(), "typing")
                telegramClient.executeAsync(action).await()

                val sendMessage = SendMessage(to.idStr(), text)
                sendMessage.parseMode = "HTML"
                telegramClient.executeAsync(sendMessage).await()
            }
            is Video -> {
                val action = SendChatAction(to.idStr(), "upload_photo")
                telegramClient.executeAsync(action).await()

                val inputFile = InputFile(tweet.content.url)
                val sendVideo = SendVideo(to.idStr(), inputFile)
                sendVideo.caption = text
                sendVideo.parseMode = "HTML"
                telegramClient.executeAsync(sendVideo).await()
            }
        }
    }

    private fun getHeaderText(
        lang: Language,
        tweet: Tweet,
        from: User
    ): String {
        return dictionary.getByKey(
            key = DictionaryKey.TWEET_HEADER,
            language = lang,
            tweet.url,
            "<a href=\"${tweet.author.url}\">${tweet.author.username}</a>",
            from.firstName
        )
    }
}

@Suppress("MagicNumber")
private fun getLimit(content: TweetContent): Int {
    return when (content) {
        is ManyMedia -> 1024
        is Photo -> 1024
        is Poll -> 4096
        Text -> 4096
        is Video -> 1024
    }
}
