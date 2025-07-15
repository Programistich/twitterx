plugins {
    alias(libs.plugins.kotlin.jvm)
}

tasks.jar {
    archiveBaseName.set("twitter-api")
}
