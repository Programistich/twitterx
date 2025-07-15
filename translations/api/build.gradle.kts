plugins {
    alias(libs.plugins.kotlin.jvm)
}

tasks.jar {
    archiveBaseName.set("translations-api")
}

dependencies {
    testImplementation(libs.kotlin.test)
}
