plugins {
    alias(libs.plugins.kotlin.jvm)
}

tasks.jar {
    archiveBaseName.set("localization-api")
}

dependencies {
    api(projects.translations.api) // For Language enum
    testImplementation(libs.kotlin.test)
}
