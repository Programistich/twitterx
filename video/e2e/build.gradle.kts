plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

application {
    mainClass = "twitterx.video.e2e.MainKt"
}

dependencies {
    implementation(projects.video.api)
    implementation(projects.video.ytdlp)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.bundles.logging.impl)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
}
