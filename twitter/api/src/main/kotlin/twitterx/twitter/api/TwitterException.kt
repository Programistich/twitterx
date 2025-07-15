package twitterx.twitter.api

public sealed class TwitterException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    public class AccountNotFoundException(
        username: String,
        cause: Throwable? = null
    ) : TwitterException("Account @$username not found", cause)

    public class TweetNotFoundException(
        tweetId: String,
        cause: Throwable? = null
    ) : TwitterException("Tweet $tweetId not found", cause)

    public class PrivateTweetException(
        tweetId: String,
        cause: Throwable? = null
    ) : TwitterException("Tweet $tweetId is private", cause)

    public class RateLimitExceededException(
        message: String = "Rate limit exceeded",
        cause: Throwable? = null
    ) : TwitterException(message, cause)

    public class ServiceUnavailableException(
        message: String = "Twitter API is unavailable",
        cause: Throwable? = null
    ) : TwitterException(message, cause)

    public class UnknownException(
        message: String = "Network error occurred",
        cause: Throwable? = null
    ) : TwitterException(message, cause)
}
