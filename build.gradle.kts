
import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.detekt)
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:${libs.versions.detekt.get()}")
}

tasks.register<Detekt>("detektFormat") {
    autoCorrect = true
    ignoreFailures = true
}

tasks.withType<Detekt> {
    // Disable caching
    outputs.upToDateWhen { false }

    reports {
        html.required.set(true)
        xml.required.set(false)
        txt.required.set(false)
    }

    setSource(files(projectDir))
    config.setFrom(files("$rootDir/detekt.yml"))

    include("**/*.kt", "**/*.kts")
    exclude(
        "**/resources/**",
        "**/build/**",
    )

    parallel = true

    buildUponDefaultConfig = true

    allRules = true

    // Target version of the generated JVM bytecode. It is used for type resolution.
    this.jvmTarget = "1.8"
}

subprojects {
    plugins.withId("org.jetbrains.kotlin.jvm") {
        extensions.configure<KotlinJvmProjectExtension>("kotlin") {
            explicitApi()
            jvmToolchain(17)
        }

        tasks.withType<Test> {
            useJUnitPlatform()
        }
    }
}
