package com.programistich.twitterx.features.twitter

import com.programistich.twitterx.core.executors.Executor
import com.programistich.twitterx.core.repos.TelegramChat
import com.programistich.twitterx.core.telegram.models.TelegramContext
import com.programistich.twitterx.core.telegram.updates.TelegramMessageUpdate
import com.programistich.twitterx.core.telegraph.TelegraphApi
import com.programistich.twitterx.core.twitter.Tweet
import com.programistich.twitterx.core.twitter.TweetContent.ManyMedia
import com.programistich.twitterx.core.twitter.TweetContent.Photo
import com.programistich.twitterx.core.twitter.TweetContent.Poll
import com.programistich.twitterx.core.twitter.TweetContent.Text
import com.programistich.twitterx.core.twitter.TweetContent.Video
import com.programistich.twitterx.core.twitter.TwitterApi
import com.programistich.twitterx.features.dict.DictionaryCache
import com.programistich.twitterx.features.dict.DictionaryKey
import com.programistich.twitterx.features.translate.GoogleTranslateApi
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
class TweetMessageExecutor(
    private val twitterApi: TwitterApi,
    private val telegraphApi: TelegraphApi,
    private val dictionary: DictionaryCache,
    private val telegramClient: TelegramClient,
    private val googleTranslateApi: GoogleTranslateApi
) : Executor<TelegramMessageUpdate> {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override val priority: Executor.Priority
        get() = Executor.Priority.HIGH

    override suspend fun canProcess(context: TelegramContext<TelegramMessageUpdate>): Boolean {
        val text = context.update.message.text?.trim() ?: return false
        return twitterApi.getTweetIds(text).isNotEmpty()
    }

    override suspend fun process(context: TelegramContext<TelegramMessageUpdate>): Result<Unit> {
        val from = context.update.message.from ?: return Result.failure(Exception("From is null"))
        val chat = context.chat ?: return Result.failure(Exception("Chat is null"))
        val text = context.update.message.text?.trim() ?: return Result.failure(Exception("Text is null"))
        val tweetId = twitterApi.getTweetIds(text).firstOrNull() ?: return Result.failure(Exception("Tweet id is null"))

        val tweet = twitterApi
            .getTweet(tweetId, chat.language.iso)
            .getOrNull()
            ?: return handleTweetException(tweetId = tweetId, to = chat)

        val result = handleTweet(tweet = tweet, to = chat, from = from)

        runCatching {
            val deleteMessage = DeleteMessage(chat.idStr(), context.update.messageId())
            telegramClient.executeAsync(deleteMessage).await()
        }

        return result
    }

    private suspend fun handleTweetException(tweetId: String, to: TelegramChat): Result<Unit> {
        logger.error("Tweet not found: $tweetId")
        val fixUrl = "https://fixupx.com/status/$tweetId/${to.language.iso}"
        val text = dictionary.getByKey(DictionaryKey.TWEET_FIX_UPX, to.language, fixUrl)

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
        val header = getHeaderText(to, tweet, from) + "\n\n"
        val content = tweet.getContent()

        val size = header.length + content.length

        val text = if (size > tweet.content.getLimit()) {
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

    private suspend fun processNotes(
        tweet: Tweet,
        to: TelegramChat,
    ): String {
        val originalNotes = tweet.note ?: return ""
        val translation = googleTranslateApi.translate(
            originalNotes, to.language.iso
        ) ?: return ""

        val content = if (tweet.translation != null && translation.to != translation.from) {
            "[${tweet.translation.to.uppercase()}] ${translation.to}\n\n[${tweet.translation.from.uppercase()}] ${translation.from}"
        } else {
            translation.from
        }

        val url = telegraphApi.createPage(
            title = "Community Note by tweet ${tweet.id}",
            content = content
        ).getOrThrow()
        return "<a href=\"$url\">📝</a>"
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

    private suspend fun getHeaderText(
        to: TelegramChat,
        tweet: Tweet,
        from: User
    ): String {
        val notes = processNotes(tweet, to)

        return dictionary.getByKey(
            key = DictionaryKey.TWEET_HEADER,
            language = to.language,
            tweet.url,
            "<a href=\"${tweet.author.url}\">${tweet.author.username}</a>",
            from.firstName
        ) + if (notes.isNotEmpty()) "\n\n$notes" else ""
    }
}
