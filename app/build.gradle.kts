plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencyManagement {
    imports {
        mavenBom("org.jetbrains.kotlinx:kotlinx-serialization-bom:${libs.versions.kotlinx.serialization.json.get()}")
    }
}

dependencies {
    implementation(projects.ai.api)
    implementation(projects.ai.google)

    implementation(projects.article.api)
    implementation(projects.article.telegraph)

    implementation(projects.localization.api)
    implementation(projects.localization.impl)

    implementation(projects.telegram)

    implementation(projects.translations.api)
    implementation(projects.translations.google)

    implementation(projects.twitter.api)
    implementation(projects.twitter.impl)
    implementation(projects.twitter.nitter)
    implementation(projects.twitter.fx)

    implementation(projects.video.api)
    implementation(projects.video.ytdlp)

    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.bundles.ktor.client)

    implementation(libs.telegram.client)
    implementation(libs.telegram.spring)

    implementation(libs.spring.web)
    implementation(libs.spring.data.jpa)

    implementation(libs.bundles.logging.api)
    implementation(libs.bundles.logging.impl)

    // Database
    runtimeOnly("org.postgresql:postgresql:42.7.5")
    runtimeOnly("org.flywaydb:flyway-database-postgresql:11.2.0")

    // Testing
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("com.h2database:h2:2.2.224")
}
