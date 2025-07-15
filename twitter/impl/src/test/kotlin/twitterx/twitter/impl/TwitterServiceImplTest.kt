package twitterx.twitter.impl

import kotlinx.coroutines.test.runTest
import twitterx.twitter.api.Tweet
import twitterx.twitter.api.TweetContentProvider
import twitterx.twitter.api.TweetIdProvider
import twitterx.twitter.api.TweetsThread
import twitterx.twitter.api.TwitterAccount
import twitterx.twitter.api.TwitterAccountProvider
import twitterx.twitter.api.TwitterException
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TwitterServiceImplTest {

    @Suppress("LongParameterList")
    private fun createTestTweet(
        id: String,
        username: String = "testuser",
        fullName: String = "Test User",
        content: String = "Test content",
        replyToTweetId: String? = null,
        retweetOfTweetId: String? = null,
        quoteTweetId: String? = null
    ): Tweet = Tweet(
        id = id,
        username = username,
        fullName = fullName,
        content = content,
        createdAt = LocalDateTime.now(),
        mediaUrls = emptyList(),
        videoUrls = emptyList(),
        replyToTweetId = replyToTweetId,
        retweetOfTweetId = retweetOfTweetId,
        quoteTweetId = quoteTweetId,
        profileImageUrl = null,
        language = "en",
        hashtags = emptyList(),
        mentions = emptyList(),
        urls = emptyList()
    )

    private fun createTestAccount(
        username: String = "testuser",
        name: String = "Test User"
    ): TwitterAccount = TwitterAccount(username, name)

    @Test
    fun `getAccount delegates to accountProvider successfully`() {
        runTest {
            val expectedAccount = createTestAccount("elonmusk", "Elon Musk")

            val mockAccountProvider = object : TwitterAccountProvider {
                override suspend fun getAccount(username: String): Result<TwitterAccount> {
                    return Result.success(expectedAccount)
                }
                override suspend fun isAccountExists(username: String): Result<Boolean> {
                    return Result.success(true)
                }
            }

            val mockIdProvider = object : TweetIdProvider {
                override suspend fun getRecentTweetIds(username: String, limit: Int): Result<List<String>> {
                    return Result.success(emptyList())
                }

                override suspend fun getTweetId(url: String): Result<String> {
                    return Result.success("123") // Simulate getting tweet ID from URL
                }
            }

            val mockTweetProvider = object : TweetContentProvider {
                override suspend fun getTweet(tweetId: String): Result<Tweet> {
                    return Result.success(createTestTweet("123"))
                }
            }

            val service = TwitterServiceImpl(mockIdProvider, mockTweetProvider, mockAccountProvider)
            val result = service.getAccount("elonmusk")

            assertTrue(result.isSuccess)
            assertEquals(expectedAccount, result.getOrThrow())
        }
    }

    @Test
    fun `getTweet delegates to tweetProvider successfully`() {
        runTest {
            val expectedTweet = createTestTweet("123456789", "testuser", "Test User", "Hello world!")

            val mockTweetProvider = object : TweetContentProvider {
                override suspend fun getTweet(tweetId: String): Result<Tweet> {
                    return if (tweetId == "123456789") {
                        Result.success(expectedTweet)
                    } else {
                        Result.failure(TwitterException.TweetNotFoundException(tweetId))
                    }
                }
            }

            val mockIdProvider = object : TweetIdProvider {
                override suspend fun getRecentTweetIds(username: String, limit: Int): Result<List<String>> {
                    return Result.success(emptyList())
                }

                override suspend fun getTweetId(url: String): Result<String> {
                    return Result.success("123456789") // Simulate getting tweet ID from URL
                }
            }

            val mockAccountProvider = object : TwitterAccountProvider {
                override suspend fun getAccount(username: String): Result<TwitterAccount> {
                    return Result.success(createTestAccount())
                }
                override suspend fun isAccountExists(username: String): Result<Boolean> {
                    return Result.success(true)
                }
            }

            val service = TwitterServiceImpl(mockIdProvider, mockTweetProvider, mockAccountProvider)
            val result = service.getTweet("123456789")

            assertTrue(result.isSuccess)
            assertEquals(expectedTweet, result.getOrThrow())
        }
    }

    @Test
    fun `getTweetThread returns Single for standalone tweet`() {
        runTest {
            val tweet = createTestTweet("123", username = "testuser", content = "Standalone tweet")

            val mockTweetProvider = object : TweetContentProvider {
                override suspend fun getTweet(tweetId: String): Result<Tweet> {
                    return Result.success(tweet)
                }
            }

            val mockIdProvider = object : TweetIdProvider {
                override suspend fun getRecentTweetIds(username: String, limit: Int): Result<List<String>> {
                    return Result.success(emptyList())
                }

                override suspend fun getTweetId(url: String): Result<String> {
                    return Result.success("123") // Simulate getting tweet ID from URL
                }
            }

            val mockAccountProvider = object : TwitterAccountProvider {
                override suspend fun getAccount(username: String): Result<TwitterAccount> {
                    return Result.success(createTestAccount())
                }
                override suspend fun isAccountExists(username: String): Result<Boolean> {
                    return Result.success(true)
                }
            }

            val service = TwitterServiceImpl(mockIdProvider, mockTweetProvider, mockAccountProvider)
            val result = service.getTweetThread("testuser", "123")

            assertTrue(result.isSuccess)
            val thread = result.getOrThrow()
            assertTrue(thread is TweetsThread.Single)
            assertEquals(tweet, thread.tweet)
        }
    }

    @Test
    fun `getTweetThread returns QuoteThread for quote tweet`() {
        runTest {
            val originalTweet = createTestTweet("111", username = "testuser", content = "Original tweet")
            val quoteTweet = createTestTweet(
                "222",
                username = "testuser",
                content = "Quote tweet",
                quoteTweetId = "111"
            )

            val mockTweetProvider = object : TweetContentProvider {
                override suspend fun getTweet(tweetId: String): Result<Tweet> {
                    return when (tweetId) {
                        "111" -> Result.success(originalTweet)
                        "222" -> Result.success(quoteTweet)
                        else -> Result.failure(TwitterException.TweetNotFoundException(tweetId))
                    }
                }
            }

            val mockIdProvider = object : TweetIdProvider {
                override suspend fun getRecentTweetIds(username: String, limit: Int): Result<List<String>> {
                    return Result.success(emptyList())
                }

                override suspend fun getTweetId(url: String): Result<String> {
                    return Result.success("222") // Simulate getting tweet ID from URL
                }
            }

            val mockAccountProvider = object : TwitterAccountProvider {
                override suspend fun getAccount(username: String): Result<TwitterAccount> {
                    return Result.success(createTestAccount())
                }
                override suspend fun isAccountExists(username: String): Result<Boolean> {
                    return Result.success(true)
                }
            }

            val service = TwitterServiceImpl(mockIdProvider, mockTweetProvider, mockAccountProvider)
            val result = service.getTweetThread("testuser", "222")

            assertTrue(result.isSuccess)
            val thread = result.getOrThrow()
            assertTrue(thread is TweetsThread.QuoteThread)
            assertEquals(originalTweet, thread.original)
            assertEquals(quoteTweet, thread.tweet)
        }
    }

    @Test
    fun `isAccountExists delegates to accountProvider successfully`() {
        runTest {
            val mockAccountProvider = object : TwitterAccountProvider {
                override suspend fun getAccount(username: String): Result<TwitterAccount> {
                    return Result.success(createTestAccount())
                }
                override suspend fun isAccountExists(username: String): Result<Boolean> {
                    return Result.success(username == "existing_user")
                }
            }

            val mockIdProvider = object : TweetIdProvider {
                override suspend fun getRecentTweetIds(username: String, limit: Int): Result<List<String>> {
                    return Result.success(emptyList())
                }

                override suspend fun getTweetId(url: String): Result<String> {
                    return Result.success("123") // Simulate getting tweet ID from URL
                }
            }

            val mockTweetProvider = object : TweetContentProvider {
                override suspend fun getTweet(tweetId: String): Result<Tweet> {
                    return Result.success(createTestTweet("123"))
                }
            }

            val service = TwitterServiceImpl(mockIdProvider, mockTweetProvider, mockAccountProvider)

            val existingResult = service.isAccountExists("existing_user")
            assertTrue(existingResult.isSuccess)
            assertTrue(existingResult.getOrThrow())

            val nonExistingResult = service.isAccountExists("non_existing_user")
            assertTrue(nonExistingResult.isSuccess)
            assertTrue(!nonExistingResult.getOrThrow())
        }
    }

    @Test
    fun `getTweetThread builds complete reply chain correctly`() {
        runTest {
            // Create a reply chain: original -> reply1 -> reply2 (where reply2 is what we're fetching)
            val originalTweet = createTestTweet(
                "100",
                username = "testuser",
                content = "Original tweet"
            ) // Root - no replyToTweetId
            val reply1 = createTestTweet("101", username = "testuser", content = "First reply", replyToTweetId = "100")
            val reply2 = createTestTweet("102", username = "testuser", content = "Second reply", replyToTweetId = "101")

            val mockTweetProvider = object : TweetContentProvider {
                override suspend fun getTweet(tweetId: String): Result<Tweet> {
                    return when (tweetId) {
                        "100" -> Result.success(originalTweet)
                        "101" -> Result.success(reply1)
                        "102" -> Result.success(reply2)
                        else -> Result.failure(TwitterException.TweetNotFoundException(tweetId))
                    }
                }
            }

            val mockIdProvider = object : TweetIdProvider {
                override suspend fun getRecentTweetIds(username: String, limit: Int): Result<List<String>> {
                    return Result.success(emptyList())
                }

                override suspend fun getTweetId(url: String): Result<String> {
                    return Result.success("102") // Simulate getting tweet ID from URL
                }
            }

            val mockAccountProvider = object : TwitterAccountProvider {
                override suspend fun getAccount(username: String): Result<TwitterAccount> {
                    return Result.success(createTestAccount())
                }
                override suspend fun isAccountExists(username: String): Result<Boolean> {
                    return Result.success(true)
                }
            }

            val service = TwitterServiceImpl(mockIdProvider, mockTweetProvider, mockAccountProvider)
            val result = service.getTweetThread("testuser", "102")

            assertTrue(result.isSuccess)
            val thread = result.getOrThrow()
            assertTrue(thread is TweetsThread.Reply)

            // Verify the complete reply chain is built correctly
            assertEquals(reply2, thread.tweet) // The tweet we started with
            assertEquals(2, thread.replies.size)
            assertEquals(reply1, thread.replies[0]) // First parent (reply1)
            assertEquals(originalTweet, thread.replies[1]) // Second parent (original tweet)
        }
    }

    @Test
    fun `getTweetThread returns RetweetThread when username differs from tweet author`() {
        runTest {
            val originalTweet = createTestTweet("123", username = "original_user", content = "Original tweet")
            val retweeterAccount = createTestAccount("retweeter_user", "Retweeter User")

            val mockTweetProvider = object : TweetContentProvider {
                override suspend fun getTweet(tweetId: String): Result<Tweet> {
                    return Result.success(originalTweet)
                }
            }

            val mockIdProvider = object : TweetIdProvider {
                override suspend fun getRecentTweetIds(username: String, limit: Int): Result<List<String>> {
                    return Result.success(emptyList())
                }

                override suspend fun getTweetId(url: String): Result<String> {
                    return Result.success("123") // Simulate getting tweet ID from URL
                }
            }

            val mockAccountProvider = object : TwitterAccountProvider {
                override suspend fun getAccount(username: String): Result<TwitterAccount> {
                    return Result.success(retweeterAccount)
                }
                override suspend fun isAccountExists(username: String): Result<Boolean> {
                    return Result.success(true)
                }
            }

            val service = TwitterServiceImpl(mockIdProvider, mockTweetProvider, mockAccountProvider)
            val result = service.getTweetThread("retweeter_user", "123")

            assertTrue(result.isSuccess)
            val thread = result.getOrThrow()
            assertTrue(thread is TweetsThread.RetweetThread)
            assertEquals(originalTweet, thread.tweet)
            assertEquals(retweeterAccount, thread.whoRetweeted)
        }
    }
}
