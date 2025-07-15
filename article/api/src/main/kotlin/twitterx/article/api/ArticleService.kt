package twitterx.article.api

public interface ArticleService {
    public suspend fun createArticle(text: String, title: String): Result<String>
}
