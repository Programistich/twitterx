package twitterx.video.ytdlp

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import twitterx.video.api.CookiesManager
import java.io.File

public class CookiesManagerImpl(
    private val config: YtDlpConfig
) : CookiesManager {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(CookiesManagerImpl::class.java)
    }

    override fun setCookies(content: String) {
        logger.debug("Setting cookies to file: {}", config.cookiesFile)
        val file = File(config.cookiesFile)

        if (!file.exists()) {
            logger.info("Creating new cookies file: {}", config.cookiesFile)
            file.parentFile?.mkdirs()
            file.createNewFile()
        }

        file.writeText(content)
        logger.info("Cookies updated successfully: {}, size={} bytes", config.cookiesFile, content.length)
    }
}
