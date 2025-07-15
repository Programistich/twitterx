package twitterx.twitter.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import twitterx.twitter.api.Tweet
import twitterx.twitter.api.TweetContentProvider
import twitterx.twitter.api.TweetIdProvider
import twitterx.twitter.api.TweetsThread
import twitterx.twitter.api.TwitterAccount
import twitterx.twitter.api.TwitterAccountProvider
import twitterx.twitter.api.TwitterService

public class TwitterServiceImpl(
    private val idProvider: TweetIdProvider,
    private val tweetProvider: TweetContentProvider,
    private val accountProvider: TwitterAccountProvider,
) : TwitterService {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(TwitterServiceImpl::class.java)
    }

    override suspend fun getAccount(username: String): Result<TwitterAccount> {
        logger.debug("Getting account: username={}", username)
        return accountProvider.getAccount(username).also { result ->
            result.fold(
                onSuccess = { logger.info("Successfully retrieved account: username={}", username) },
                onFailure = {
                    logger.warn(
                        "Failed to retrieve account: username={}, error={}",
                        username,
                        it::class.simpleName
                    )
                }
            )
        }
    }

    override suspend fun getTweet(tweetId: String): Result<Tweet> {
        logger.debug("Getting tweet: tweetId={}", tweetId)
        return tweetProvider.getTweet(tweetId).also { result ->
            result.fold(
                onSuccess = { logger.info("Successfully retrieved tweet: tweetId={}", tweetId) },
                onFailure = {
                    logger.warn(
                        "Failed to retrieve tweet: tweetId={}, error={}",
                        tweetId,
                        it::class.simpleName
                    )
                }
            )
        }
    }

    override suspend fun getRecentTweetIds(username: String, limit: Int): Result<List<String>> {
        logger.debug("Getting recent tweet IDs: username={}, limit={}", username, limit)
        return idProvider.getRecentTweetIds(username, limit).also { result ->
            result.fold(
                onSuccess = {
                    logger.info(
                        "Successfully retrieved {} recent tweet IDs: username={}",
                        it.size,
                        username
                    )
                },
                onFailure = {
                    logger.warn(
                        "Failed to retrieve recent tweet IDs: username={}, error={}",
                        username,
                        it::class.simpleName
                    )
                }
            )
        }
    }

    override suspend fun getTweetId(url: String): Result<String> {
        logger.debug("Extracting tweet ID from URL: url={}", url)
        return idProvider.getTweetId(url).also { result ->
            result.fold(
                onSuccess = {
                    logger.debug(
                        "Successfully extracted tweet ID: url={}, tweetId={}",
                        url,
                        it
                    )
                },
                onFailure = {
                    logger.warn(
                        "Failed to extract tweet ID: url={}, error={}",
                        url,
                        it::class.simpleName
                    )
                }
            )
        }
    }

    override suspend fun getUsername(url: String): Result<String> {
        logger.debug("Extracting username from URL: url={}", url)
        return idProvider.getUsername(url).also { result ->
            result.fold(
                onSuccess = {
                    logger.debug(
                        "Successfully extracted username: url={}, username={}",
                        url,
                        it
                    )
                },
                onFailure = {
                    logger.warn(
                        "Failed to extract username: url={}, error={}",
                        url,
                        it::class.simpleName
                    )
                }
            )
        }
    }

    override suspend fun isAccountExists(username: String): Result<Boolean> {
        logger.debug("Checking account existence: username={}", username)
        return accountProvider.isAccountExists(username).also { result ->
            result.fold(
                onSuccess = {
                    logger.info(
                        "Account existence check: username={}, exists={}",
                        username,
                        it
                    )
                },
                onFailure = {
                    logger.warn(
                        "Failed to check account existence: username={}, error={}",
                        username,
                        it::class.simpleName
                    )
                }
            )
        }
    }

    override suspend fun getTweetThread(
        username: String,
        tweetId: String
    ): Result<TweetsThread> = runCatching {
        logger.debug("Getting tweet thread: username={}, tweetId={}", username, tweetId)

        val tweet = tweetProvider.getTweet(tweetId).getOrThrow()
        if (tweet.username != username) {
            logger.info(
                "Tweet is a retweet: originalAuthor={}, retweetedBy={}, tweetId={}",
                tweet.username,
                username,
                tweetId
            )
            val account = accountProvider.getAccount(username).getOrThrow()

            return@runCatching TweetsThread.RetweetThread(
                tweet = tweet,
                whoRetweeted = account
            )
        }

        when {
            tweet.replyToTweetId != null -> {
                logger.debug("Processing reply thread: tweetId={}, replyToTweetId={}", tweetId, tweet.replyToTweetId)
                val replies = mutableListOf<Tweet>()
                var currentTweetId: String? = tweet.replyToTweetId

                while (currentTweetId != null) {
                    val parentTweet = tweetProvider.getTweet(currentTweetId).getOrThrow()
                    replies.add(parentTweet)
                    currentTweetId = parentTweet.replyToTweetId
                }

                val last = replies.lastOrNull()
                val quoteTweet = last?.quoteTweetId?.let { tweetProvider.getTweet(it) }?.getOrNull()

                logger.info("Built reply thread: tweetId={}, replyChainLength={}", tweetId, replies.size)
                TweetsThread.Reply(
                    tweet = tweet,
                    replies = replies,
                    quotedTweet = quoteTweet
                )
            }
            tweet.quoteTweetId != null -> {
                logger.debug("Processing quote thread: tweetId={}, quoteTweetId={}", tweetId, tweet.quoteTweetId)
                val original = tweetProvider.getTweet(tweet.quoteTweetId!!).getOrThrow()
                logger.info("Built quote thread: tweetId={}, originalTweetId={}", tweetId, tweet.quoteTweetId)
                TweetsThread.QuoteThread(
                    tweet = tweet,
                    original = original
                )
            }
            else -> {
                logger.debug("Single tweet thread: tweetId={}", tweetId)
                TweetsThread.Single(tweet)
            }
        }
    }
}
