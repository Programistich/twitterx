package twitter.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
public open class TwitterXApplication

public fun main() {
    runApplication<TwitterXApplication>()
}
