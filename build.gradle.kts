import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    kotlin("multiplatform") version "1.5.30-RC"
    kotlin("plugin.serialization") version "1.5.30-RC"
    id("org.jlleitschuh.gradle.ktlint") version "10.1.0"
    id("maven-publish")
}

group = "io.beatmaps"
version = System.getenv("BUILD_NUMBER")?.let { "1.0.$it" } ?: "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "15"
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
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.1.1")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
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
