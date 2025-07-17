package twitter.app.features.twitter

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import twitter.app.repo.SentTweet
import twitter.app.repo.SentTweetRepository
import twitter.app.repo.TelegramChat
import twitter.app.repo.TelegramChatRepository
import twitter.app.repo.TweetTracking
import twitter.app.repo.TweetTrackingRepository
import twitterx.twitter.api.Tweet
import twitterx.twitter.api.TweetsThread
import twitterx.twitter.api.TwitterService
import java.time.LocalDateTime

@Service
public open class ElonMuskMonitoringService(
    private val twitterService: TwitterService,
    private val telegramChatRepository: TelegramChatRepository,
    private val tweetTrackingRepository: TweetTrackingRepository,
    private val sentTweetRepository: SentTweetRepository,
    private val tweetSender: TweetSender
) {

    private val logger = LoggerFactory.getLogger(ElonMuskMonitoringService::class.java)
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private companion object {
        private const val ELON_MUSK_USERNAME = "elonmusk"
    }

    @Scheduled(fixedRate = 300000)
    @Transactional
    public open fun checkForNewTweets() {
        coroutineScope.launch {
            checkForNewTweetsInternal()
        }
    }

    private suspend fun checkForNewTweetsInternal() = withContext(Dispatchers.IO) {
        logger.info("Starting to check for new tweets from @$ELON_MUSK_USERNAME")

        try {
            val tweetTracking = tweetTrackingRepository.findByUsername(ELON_MUSK_USERNAME)
                ?: TweetTracking(
                    username = ELON_MUSK_USERNAME,
                    lastTweetId = "",
                    lastChecked = LocalDateTime.now(),
                    isActive = true
                ).also { tweetTrackingRepository.save(it) }

            if (!tweetTracking.isActive) {
                logger.info("Monitoring for @$ELON_MUSK_USERNAME is disabled")
                return@withContext
            }

            val tweetIdsResult = twitterService.getRecentTweetIds(ELON_MUSK_USERNAME, 10)

            if (tweetIdsResult.isFailure) {
                logger.error("Failed to get tweet IDs from @$ELON_MUSK_USERNAME: ${tweetIdsResult.exceptionOrNull()}")
                return@withContext
            }

            val tweetIds = tweetIdsResult.getOrNull() ?: run {
                logger.warn("No tweet IDs received from @$ELON_MUSK_USERNAME")
                return@withContext
            }

            if (tweetIds.isEmpty()) {
                logger.info("No tweets from @$ELON_MUSK_USERNAME")
                return@withContext
            }

            val allTweets = tweetIds.mapNotNull { tweetId ->
                twitterService.getTweet(tweetId).getOrNull()
            }

            if (allTweets.isEmpty()) {
                logger.info("No tweets could be loaded from @$ELON_MUSK_USERNAME")
                return@withContext
            }

            val newTweets = if (tweetTracking.lastTweetId.isEmpty()) {
                allTweets.take(1)
            } else {
                allTweets.takeWhile { it.id != tweetTracking.lastTweetId }
            }

            if (newTweets.isEmpty()) {
                logger.info("No new tweets from @$ELON_MUSK_USERNAME")
                tweetTracking.lastChecked = LocalDateTime.now()
                tweetTrackingRepository.save(tweetTracking)
                return@withContext
            }

            val subscribers = telegramChatRepository.findAllElonMuskSubscribers()

            if (subscribers.isEmpty()) {
                logger.info("No subscribers found for @$ELON_MUSK_USERNAME")
                return@withContext
            }

            logger.info("Found ${newTweets.size} new tweets and ${subscribers.size} subscribers")

            newTweets.forEach { tweet ->
                subscribers.forEach { subscriber ->
                    try {
                        sendTweetThreadToSubscriber(tweet, subscriber)
                    } catch (e: Exception) {
                        logger.error("Failed to send tweet ${tweet.id} to subscriber ${subscriber.id}: ${e.message}")
                    }
                }
            }

            if (newTweets.isNotEmpty()) {
                tweetTracking.lastTweetId = newTweets.first().id
                tweetTracking.lastChecked = LocalDateTime.now()
                tweetTrackingRepository.save(tweetTracking)
            }

            logger.info("Finished processing tweets from @$ELON_MUSK_USERNAME")
        } catch (e: Exception) {
            logger.error("Error in checkForNewTweets: ${e.message}", e)
        }
    }

    private suspend fun sendTweetThreadToSubscriber(tweet: Tweet, subscriber: TelegramChat) {
        try {
            val threadResult = twitterService.getTweetThread(ELON_MUSK_USERNAME, tweet.id)

            if (threadResult.isFailure) {
                logger.error("Failed to get tweet thread for ${tweet.id}: ${threadResult.exceptionOrNull()}")
                return
            }

            val tweetsThread = threadResult.getOrNull() ?: run {
                logger.warn("No thread found for tweet ${tweet.id}")
                return
            }

            val existingSentTweet = sentTweetRepository.findByTweetIdAndChatId(tweet.id, subscriber.id)
            if (existingSentTweet != null) {
                logger.debug("Tweet ${tweet.id} already sent to subscriber ${subscriber.id}")
                return
            }

            // Check if this is a reply and if the parent tweet exists in the database
            val replyToMessageId = if (tweetsThread is TweetsThread.Reply) {
                val parentTweetId = tweet.replyToTweetId
                if (parentTweetId != null) {
                    val parentTweet = sentTweetRepository.findByTweetIdAndChatId(parentTweetId, subscriber.id)
                    if (parentTweet != null) {
                        logger.debug("Found parent tweet $parentTweetId for reply ${tweet.id}, using message ID ${parentTweet.messageId}")
                        parentTweet.messageId
                    } else {
                        logger.debug("Parent tweet $parentTweetId not found for reply ${tweet.id}")
                        null
                    }
                } else {
                    null
                }
            } else {
                null
            }

            val messageId = tweetSender.sendTweetThread(
                tweetsThread,
                subscriber.id,
                subscriber.language,
                replyToMessageId
            )

            if (messageId != null) {
                val sentTweet = SentTweet(
                    tweetId = tweet.id,
                    chatId = subscriber.id,
                    messageId = messageId,
                    parentTweetId = tweet.replyToTweetId,
                    threadId = tweet.id,
                    isMainTweet = true
                )
                sentTweetRepository.save(sentTweet)
                logger.debug("Sent tweet thread ${tweet.id} to subscriber ${subscriber.id}")
            } else {
                logger.warn(
                    "Failed to send tweet thread ${tweet.id} to subscriber ${subscriber.id} - no message ID returned"
                )
            }
        } catch (e: Exception) {
            logger.error("Failed to send tweet thread ${tweet.id} to subscriber ${subscriber.id}: ${e.message}")
            throw e
        }
    }
}
