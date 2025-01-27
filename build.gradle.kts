plugins {
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
}

group = "com.programistich"
version = "0.0.1-SNAPSHOT"

kotlin {
    jvmToolchain(17)
}

repositories {
    mavenCentral()
}

dependencies {
    // Telegram
    implementation("org.telegram:telegrambots-springboot-longpolling-starter:8.2.0")
    implementation("org.telegram:telegrambots-client:8.2.0")

    // Spring
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Database
    runtimeOnly("org.postgresql:postgresql:42.7.5")
    runtimeOnly("org.flywaydb:flyway-database-postgresql:11.2.0")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")

    implementation("commons-codec:commons-codec:1.17.2")

    // Ktor client
    val version = "3.0.3"
    implementation("io.ktor:ktor-client-core:$version")
    implementation("io.ktor:ktor-client-serialization:$version")
    implementation("io.ktor:ktor-client-logging:$version")
    implementation("io.ktor:ktor-client-cio:$version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$version")
    implementation("io.ktor:ktor-client-content-negotiation:$version")

    // Toml
    implementation("org.tomlj:tomlj:1.1.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
