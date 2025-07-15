package twitterx.article.telegraph

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonArray
import org.slf4j.LoggerFactory
import twitterx.article.api.ArticleApiException
import twitterx.article.api.ArticleCreationException

public class TelegraphClient(
    private val httpClient: HttpClient
) {
    private val logger = LoggerFactory.getLogger(TelegraphClient::class.java)

    public companion object {
        private const val BASE_URL = "https://api.telegra.ph"
    }

    public suspend fun createAccount(request: CreateAccountRequest): TelegraphAccount {
        logger.debug("Creating Telegraph account with short_name: ${request.shortName}")

        val response: TelegraphResponse<TelegraphAccount> = httpClient.post("$BASE_URL/createAccount") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(
                FormDataContent(
                    Parameters.build {
                        append("short_name", request.shortName)
                        request.authorName?.let { append("author_name", it) }
                        request.authorUrl?.let { append("author_url", it) }
                    }
                )
            )
        }.body()

        return if (response.ok && response.result != null) {
            logger.debug("Successfully created Telegraph account: ${response.result.shortName}")
            response.result
        } else {
            val error = response.error ?: "Unknown error creating account"
            logger.error("Failed to create Telegraph account: $error")
            throw ArticleApiException("Failed to create Telegraph account: $error")
        }
    }

    public suspend fun createPage(request: CreatePageRequest): TelegraphPage {
        logger.debug("Creating Telegraph page with title: ${request.title}")

        val response: TelegraphResponse<TelegraphPage> = httpClient.post("$BASE_URL/createPage") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(
                FormDataContent(
                    Parameters.build {
                        append("access_token", request.accessToken)
                        append("title", request.title)
                        val contentJsonArray = JsonArray(request.content.map { it.toJsonElement() })
                        append("content", contentJsonArray.toString())
                        request.authorName?.let { append("author_name", it) }
                        request.authorUrl?.let { append("author_url", it) }
                        append("return_content", request.returnContent.toString())
                    }
                )
            )
        }.body()

        return if (response.ok && response.result != null) {
            logger.debug("Successfully created Telegraph page: ${response.result.url}")
            response.result
        } else {
            val error = response.error ?: "Unknown error creating page"
            logger.error("Failed to create Telegraph page: $error")
            throw ArticleCreationException("Failed to create Telegraph page: $error")
        }
    }
}
