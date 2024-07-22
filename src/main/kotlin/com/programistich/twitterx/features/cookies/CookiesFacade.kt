package com.programistich.twitterx.features.cookies

import org.springframework.stereotype.Component
import java.io.File

@Component
class CookiesFacade {
    fun getCookiesFile(): File {
        val getSpringBootFolder = { File("").absolutePath }
        val cookiesFile = File(getSpringBootFolder(), FILE_NAME)
        return cookiesFile
    }

    companion object {
        const val FILE_NAME = "cookies.txt"
    }
}
