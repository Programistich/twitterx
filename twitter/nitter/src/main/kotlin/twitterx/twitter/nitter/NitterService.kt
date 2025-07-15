package twitterx.twitter.nitter

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import twitterx.twitter.api.TweetIdProvider
import twitterx.twitter.api.TwitterAccount
import twitterx.twitter.api.TwitterAccountProvider
import twitterx.twitter.api.TwitterException
import java.util.regex.Pattern

public class NitterService(
    public val httpClient: HttpClient,
    public val nitterBaseUrl: String
) : TweetIdProvider, TwitterAccountProvider {

    private val rssParser = RSSParser()

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(NitterService::class.java)
    }

    override suspend fun getRecentTweetIds(
        username: String,
        limit: Int
    ): Result<List<String>> = runCatching {
        logger.debug("Fetching recent tweet IDs from Nitter: username={}, limit={}", username, limit)
        val response = httpClient.get("$nitterBaseUrl/$username/with_replies/rss")

        if (response.status == HttpStatusCode.NotFound) {
            logger.info("Account not found on Nitter: username={}", username)
            throw TwitterException.AccountNotFoundException(username)
        }

        if (response.status == HttpStatusCode.TooManyRequests) {
            logger.warn("Rate limit exceeded for Nitter RSS: username={}", username)
            throw TwitterException.RateLimitExceededException()
        }

        if (!response.status.isSuccess()) {
            logger.error("Failed to fetch RSS feed from Nitter: username={}, status={}", username, response.status)
            throw TwitterException.ServiceUnavailableException(
                "Failed to fetch RSS feed: ${response.status}"
            )
        }

        val rssXml = response.bodyAsText()
        val rssFeed = rssParser.parseRSSFeed(rssXml)

        val tweetIds = rssFeed.items
            .take(limit)
            .map { getTweetId(it.link) }
            .map { result ->
                result.getOrThrow()
            }

        logger.info("Successfully fetched {} tweet IDs from Nitter: username={}", tweetIds.size, username)
        tweetIds
    }

    override suspend fun getTweetId(url: String): Result<String> {
        val tweetIdPattern = Pattern.compile("/status/([0-9]+)")
        val matcher = tweetIdPattern.matcher(url)
        return runCatching {
            if (matcher.find()) {
                val tweetId = matcher.group(1) ?: throw TwitterException.TweetNotFoundException(url)
                logger.debug("Extracted tweet ID from URL: tweetId={}, url={}", tweetId, url)
                tweetId
            } else {
                logger.warn("Failed to extract tweet ID from URL: url={}", url)
                throw TwitterException.TweetNotFoundException(url)
            }
        }
    }

    override suspend fun getUsername(url: String): Result<String> {
        val patterns = listOf(
            "https?://(?:www\\.|mobile\\.)?(?:twitter\\.com|x\\.com)/@?([a-zA-Z0-9_]+)".toRegex(),
            "https?://(?:www\\.|mobile\\.)?(?:twitter\\.com|x\\.com)/([a-zA-Z0-9_]+)/status/".toRegex()
        )

        for (pattern in patterns) {
            val match = pattern.find(url)
            if (match != null) {
                return Result.success(match.groupValues[1])
            }
        }
        return Result.failure(
            TwitterException.AccountNotFoundException("Failed to extract username from URL: $url")
        )
    }

    override suspend fun isAccountExists(
        username: String
    ): Result<Boolean> = runCatching {
        logger.debug("Checking if account exists on Nitter: username={}", username)
        val response = httpClient.get("$nitterBaseUrl/$username/with_replies/rss")
        val exists = response.status.isSuccess()
        logger.info("Account existence check result: username={}, exists={}", username, exists)
        exists
    }

    override suspend fun getAccount(
        username: String
    ): Result<TwitterAccount> = runCatching {
        logger.debug("Fetching account info from Nitter: username={}", username)
        val response = httpClient.get("$nitterBaseUrl/$username/with_replies/rss")

        if (response.status == HttpStatusCode.NotFound) {
            logger.info("Account not found when fetching info: username={}", username)
            throw TwitterException.AccountNotFoundException(username)
        }

        if (response.status == HttpStatusCode.TooManyRequests) {
            logger.warn("Rate limit exceeded when fetching account info: username={}", username)
            throw TwitterException.RateLimitExceededException()
        }

        if (!response.status.isSuccess()) {
            logger.error("Failed to fetch account info from Nitter: username={}, status={}", username, response.status)
            throw TwitterException.ServiceUnavailableException(
                "Failed to fetch account info: ${response.status}"
            )
        }

        val rssXml = response.bodyAsText()
        val rssFeed = rssParser.parseRSSFeed(rssXml)

        val account = TwitterAccount(
            username = username,
            name = parseName(rssFeed),
        )

        logger.info("Successfully fetched account info: username={}, name={}", username, account.name)
        account
    }

    /**
     *  <title>Elon Musk / @elonmusk</title>
     *  *  Parses the name from the RSS feed title.
     *  *  Example: "Elon Musk / @elonmusk" -> "Elon Musk"
     */
    private fun parseName(feed: RSSFeed): String {
        val name = feed.title
        val username = feed.link.substringAfterLast("/")
        return name.replace(" / @$username", "")
    }
}
