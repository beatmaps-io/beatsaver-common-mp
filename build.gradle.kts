import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    kotlin("multiplatform") version "1.5.30"
    kotlin("plugin.serialization") version "1.5.30"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.0"
    id("maven-publish")
}

val exposedVersion: String by project
val ktorVersion: String by project
group = "io.beatmaps"
version = System.getenv("BUILD_NUMBER")?.let { "1.0.$it" } ?: "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(16))
    }
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "16"
        }
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }
    js(IR) {
        browser()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0-RC")
            }
        }
        val jvmMain by getting {
            repositories {
                mavenCentral()
                maven { url = uri("https://jitpack.io") }
                maven { url = uri("https://artifactory.kirkstall.top-cat.me") }
            }
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.0")

                // Database library
                implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
                implementation("com.zaxxer:HikariCP:3.4.2")

                implementation("io.ktor:ktor-client-apache:$ktorVersion")
                implementation("io.ktor:ktor-client-serialization:$ktorVersion")
                implementation("io.ktor:ktor-client-jackson:$ktorVersion")

                implementation("org.postgresql:postgresql:42.1.4")
                implementation("com.github.JUtupe:ktor-rabbitmq:0.2.0")
                implementation("com.rabbitmq:amqp-client:5.9.0")

                implementation("org.apache.commons:commons-email:1.5")

                // Serialization
                implementation("io.ktor:ktor-jackson:$ktorVersion")
                implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.6.1")

                // Metrics
                implementation("io.ktor:ktor-metrics-micrometer:$ktorVersion")
                implementation("io.micrometer:micrometer-core:1.7.3")
                implementation("io.micrometer:micrometer-registry-influx:1.7.3")
                implementation("io.micrometer:micrometer-registry-elastic:1.7.3")
                implementation("nl.basjes.parse.useragent:yauaa:6.0")
                implementation("org.apache.logging.log4j:log4j-api:2.15.0") // Required by yauaa at runtime
                implementation("com.maxmind.geoip2:geoip2:2.15.0")

                // Multimedia
                implementation("org.jaudiotagger:jaudiotagger:2.0.1")
                implementation("net.coobird:thumbnailator:0.4.13")
                implementation("com.twelvemonkeys.imageio:imageio-jpeg:3.6.1")
                implementation("org.sejda.imageio:webp-imageio:0.1.6")
                implementation("nwaldispuehl:java-lame:3.98.4")

                implementation("org.valiktor:valiktor-core:0.12.0")
            }
        }
    }
}

ktlint {
    reporters {
        reporter(ReporterType.CHECKSTYLE)
    }
}

publishing {
    repositories {
        maven {
            name = "reposilite"
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
            url = uri("https://artifactory.kirkstall.top-cat.me/")
        }
    }
}
