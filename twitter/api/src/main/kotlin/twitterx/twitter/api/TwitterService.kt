package twitterx.twitter.api

public interface TwitterService : TweetIdProvider, TweetContentProvider, TwitterAccountProvider {
    public override suspend fun getAccount(
        username: String
    ): Result<TwitterAccount>

    public override suspend fun getTweet(
        tweetId: String
    ): Result<Tweet>

    public override suspend fun getRecentTweetIds(
        username: String,
        limit: Int
    ): Result<List<String>>

    public suspend fun getTweetThread(
        username: String,
        tweetId: String
    ): Result<TweetsThread>

    public override suspend fun getTweetId(
        url: String
    ): Result<String>

    public override suspend fun getUsername(
        url: String
    ): Result<String>

    public override suspend fun isAccountExists(username: String): Result<Boolean>
}

public interface TweetIdProvider {
    public suspend fun getRecentTweetIds(
        username: String,
        limit: Int
    ): Result<List<String>>

    public suspend fun getTweetId(
        url: String
    ): Result<String>

    public suspend fun getUsername(
        url: String
    ): Result<String>
}

public interface TweetContentProvider {
    public suspend fun getTweet(tweetId: String): Result<Tweet>
}

public interface TwitterAccountProvider {
    public suspend fun getAccount(username: String): Result<TwitterAccount>
    public suspend fun isAccountExists(username: String): Result<Boolean>
}
