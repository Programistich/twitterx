package twitterx.article.telegraph

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import twitterx.article.api.ArticleApiException
import twitterx.article.api.ArticleContentTooLongException
import twitterx.article.api.ArticleCreationException
import twitterx.article.api.ArticleInvalidContentException
import twitterx.article.api.ArticleService
import java.util.UUID

public class TelegraphService(
    private val httpClient: HttpClient,
    private val accountShortName: String = "TwitterX-${UUID.randomUUID().toString().take(ACCOUNT_SHORT_NAME_LENGTH)}",
    private val authorName: String = "TwitterX Bot",
    private val authorUrl: String? = null
) : ArticleService {

    private val logger = LoggerFactory.getLogger(TelegraphService::class.java)
    private val telegraphClient = TelegraphClient(httpClient)
    private val textToDomConverter = TextToDomConverter()

    @Volatile
    private var cachedAccount: TelegraphAccount? = null

    public companion object {
        private const val ACCOUNT_SHORT_NAME_LENGTH = 8
        private const val REQUEST_TIMEOUT_MILLIS = 30000L
        private const val CONNECT_TIMEOUT_MILLIS = 10000L

        public fun createDefaultHttpClient(): HttpClient = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            install(Logging) {
                level = LogLevel.INFO
            }
            install(HttpTimeout) {
                requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
                connectTimeoutMillis = CONNECT_TIMEOUT_MILLIS
            }
        }
    }

    override suspend fun createArticle(text: String, title: String): Result<String> {
        return try {
            logger.info("Creating article with title: $title")

            // Validate input
            val validatedTitle = textToDomConverter.validateTitle(title)
            val contentNodes = textToDomConverter.convertTextToNodes(text)

            // Get or create account
            val account = getOrCreateAccount()

            // Create page
            val pageRequest = CreatePageRequest(
                accessToken = account.accessToken!!,
                title = validatedTitle,
                content = contentNodes,
                authorName = authorName,
                authorUrl = authorUrl,
                returnContent = false
            )

            val page = telegraphClient.createPage(pageRequest)

            logger.info("Successfully created article: ${page.url}")
            Result.success(page.url)
        } catch (e: ArticleApiException) {
            logger.error("Failed to create article: ${e.message}", e)
            Result.failure(e)
        } catch (e: ArticleCreationException) {
            logger.error("Failed to create article: ${e.message}", e)
            Result.failure(e)
        } catch (e: ArticleContentTooLongException) {
            logger.error("Failed to create article: ${e.message}", e)
            Result.failure(e)
        } catch (e: ArticleInvalidContentException) {
            logger.error("Failed to create article: ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun getOrCreateAccount(): TelegraphAccount {
        // Return cached account if available
        cachedAccount?.let { account ->
            if (account.accessToken != null) {
                logger.debug("Using cached Telegraph account: ${account.shortName}")
                return account
            }
        }

        // Create new account
        logger.info("Creating new Telegraph account: $accountShortName")
        val accountRequest = CreateAccountRequest(
            shortName = accountShortName,
            authorName = authorName,
            authorUrl = authorUrl
        )

        val account = telegraphClient.createAccount(accountRequest)

        if (account.accessToken == null) {
            throw ArticleApiException("Created Telegraph account without access token")
        }

        // Cache the account
        cachedAccount = account
        logger.info("Successfully created Telegraph account: ${account.shortName}")

        return account
    }
}
