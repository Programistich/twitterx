plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    testImplementation(projects.article.api)
    testImplementation(projects.article.telegraph)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.bundles.logging.impl)
}
