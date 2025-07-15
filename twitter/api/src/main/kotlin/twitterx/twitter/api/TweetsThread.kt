package twitterx.twitter.api

/**
 * Represents a thread of tweets, which can be a single tweet, a reply with its replies,
 * a retweet thread, or a quote thread.
 * -----
 * In [Single] is look like this:
 * - A
 * @property tweet is original tweet A.
 * -----
 * In [Reply] is look like this:
 * - A
 * -- B
 * --- C
 * ---- tweet
 *  @property tweet is original tweet in thread.
 *  @property replies is list of replies to the tweet in thread. [C, B, A]
 * -----
 * In [RetweetThread] is look like this:
 * - A
 * -- B - retweet of A
 * @property tweet is tweet B
 * @property original is author of tweet A
 * -----
 * In [QuoteThread] is look like this:
 * - A
 * -- B - quote of A
 * @property tweet is tweet B
 * @property original is original tweet A
 */
public sealed class TweetsThread(tweet: Tweet) {
    public data class Single(val tweet: Tweet) : TweetsThread(tweet)

    public data class Reply(
        public val tweet: Tweet,
        public val replies: List<Tweet>,
        public val quotedTweet: Tweet?,
    ) : TweetsThread(tweet)

    public data class RetweetThread(
        public val tweet: Tweet,
        public val whoRetweeted: TwitterAccount
    ) : TweetsThread(tweet)

    public data class QuoteThread(
        public val tweet: Tweet,
        public val original: Tweet
    ) : TweetsThread(tweet)
}
