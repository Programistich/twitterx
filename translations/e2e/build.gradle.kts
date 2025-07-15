plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(projects.translations.api)
    implementation(projects.translations.google)

    implementation(libs.bundles.ktor.client)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.bundles.logging.impl)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.kotlinx.coroutines.test)
}
