plugins {
    alias(libs.plugins.kotlin.jvm)
}

tasks.jar {
    archiveBaseName.set("article-api")
}
