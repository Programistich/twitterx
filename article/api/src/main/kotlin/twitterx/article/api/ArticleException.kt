package twitterx.article.api

public sealed class ArticleException(message: String, cause: Throwable? = null) : Exception(message, cause)

public class ArticleCreationException(message: String, cause: Throwable? = null) : ArticleException(message, cause)

public class ArticleApiException(message: String, cause: Throwable? = null) : ArticleException(message, cause)

public class ArticleContentTooLongException(message: String, cause: Throwable? = null) : ArticleException(
    message,
    cause
)

public class ArticleInvalidContentException(message: String, cause: Throwable? = null) : ArticleException(
    message,
    cause
)
