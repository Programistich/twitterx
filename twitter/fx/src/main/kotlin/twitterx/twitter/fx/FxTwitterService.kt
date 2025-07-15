package twitterx.twitter.fx

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import twitterx.twitter.api.Tweet
import twitterx.twitter.api.TweetContentProvider
import twitterx.twitter.api.TwitterException

public class FxTwitterService(
    public val httpClient: HttpClient,
    public val baseUrl: String
) : TweetContentProvider {

    private companion object {
        const val SUCCESS_CODE = 200
        private val logger: Logger = LoggerFactory.getLogger(FxTwitterService::class.java)
    }

    override suspend fun getTweet(tweetId: String): Result<Tweet> {
        logger.debug("Fetching tweet from FxTwitter: tweetId={}", tweetId)
        return getFXTweet(tweetId).map {
            logger.info("Successfully fetched tweet: tweetId={}", tweetId)
            FXTwitterConverter.convert(it, tweetId)
        }
    }

    private suspend fun getFXTweet(tweetId: String): Result<FxTweet> = runCatching {
        val response = httpClient.get("$baseUrl/status/$tweetId")

        if (!response.status.isSuccess()) {
            logger.warn("FxTwitter API returned error status: {} for tweetId={}", response.status, tweetId)
            when (response.status) {
                HttpStatusCode.NotFound -> {
                    logger.info("Tweet not found: tweetId={}", tweetId)
                    throw TwitterException.TweetNotFoundException(tweetId)
                }
                HttpStatusCode.Unauthorized -> {
                    logger.info("Tweet is private: tweetId={}", tweetId)
                    throw TwitterException.PrivateTweetException(tweetId)
                }
                HttpStatusCode.TooManyRequests -> {
                    logger.warn("Rate limit exceeded for FxTwitter API")
                    throw TwitterException.RateLimitExceededException()
                }
                else -> {
                    logger.error("FxTwitter API error: status={}, tweetId={}", response.status, tweetId)
                    throw TwitterException.ServiceUnavailableException(
                        "FxTwitter API error: ${response.status}"
                    )
                }
            }
        }

        val fxResponse: FxTwitterResponse = response.body()

        if (fxResponse.code != SUCCESS_CODE) {
            logger.warn(
                "FxTwitter response error: code={}, message={}, tweetId={}",
                fxResponse.code,
                fxResponse.message,
                tweetId
            )
            when (fxResponse.message) {
                "NOT_FOUND" -> {
                    logger.info("Tweet not found via FxTwitter response: tweetId={}", tweetId)
                    throw TwitterException.TweetNotFoundException(tweetId)
                }
                "PRIVATE_TWEET" -> {
                    logger.info("Tweet is private via FxTwitter response: tweetId={}", tweetId)
                    throw TwitterException.PrivateTweetException(tweetId)
                }
                "API_FAIL" -> {
                    logger.error("FxTwitter API failure: tweetId={}", tweetId)
                    throw TwitterException.ServiceUnavailableException()
                }
                else -> {
                    logger.error("FxTwitter unknown error: message={}, tweetId={}", fxResponse.message, tweetId)
                    throw TwitterException.ServiceUnavailableException(
                        "FxTwitter error: ${fxResponse.message}"
                    )
                }
            }
        }

        if (fxResponse.tweet == null) {
            logger.error("FxTwitter response contains null tweet: tweetId={}", tweetId)
            throw TwitterException.TweetNotFoundException(tweetId)
        }

        return@runCatching fxResponse.tweet
    }
}
