plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(projects.twitter.impl)
    implementation(projects.twitter.api)
    implementation(projects.twitter.fx)
    implementation(projects.twitter.nitter)

    implementation(libs.bundles.ktor.client)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.bundles.logging.impl)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.kotlinx.coroutines.test)
}
