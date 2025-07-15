plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

tasks.jar {
    archiveBaseName.set("telegram")
}

dependencies {
    api(projects.translations.api)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.bundles.logging.api)
}
