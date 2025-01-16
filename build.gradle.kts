import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    kotlin("multiplatform") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
    id("org.jlleitschuh.gradle.ktlint") version "11.5.1"
    id("maven-publish")
}

val exposedVersion: String by project
val ktorVersion: String by project
group = "io.beatmaps"
version = System.getenv("BUILD_NUMBER")?.let { "1.0.$it" } ?: "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://artifactory.kirkstall.top-cat.me") }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(16))
    }
    jvm {
        compilerOptions.jvmTarget = JvmTarget.JVM_16
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }
    js(IR) {
        browser()
    }

    sourceSets {
        val commonMain by getting {
            with(languageSettings) {
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
            }
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jsMain by getting {
            with(languageSettings) {
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
        val jvmMain by getting {
            repositories {
                mavenCentral()
                maven { url = uri("https://jitpack.io") }
                maven { url = uri("https://artifactory.kirkstall.top-cat.me") }
            }
            with(languageSettings) {
                optIn("kotlinx.coroutines.DelicateCoroutinesApi")
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
                optIn("kotlinx.serialization.InternalSerializationApi")
            }
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.6.4")

                // Database library
                implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
                implementation("com.zaxxer:HikariCP:3.4.2")

                implementation("io.ktor:ktor-client-apache:$ktorVersion")
                implementation("io.ktor:ktor-client-serialization:$ktorVersion")

                implementation("org.postgresql:postgresql:42.7.3")
                implementation("pl.jutupe:ktor-rabbitmq:0.5.19")
                implementation("com.rabbitmq:amqp-client:5.21.0")
                implementation("org.apache.solr:solr-solrj:9.7.0")
                implementation("com.michael-bull.kotlin-retry:kotlin-retry:2.0.1")

                implementation("org.apache.commons:commons-email:1.5")

                // Serialization
                implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")
                implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.2")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")

                // Metrics
                implementation("io.ktor:ktor-server-metrics-micrometer:$ktorVersion")
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

                implementation("net.lingala.zip4j:zip4j:2.11.6-SNAPSHOT")

                implementation("org.valiktor:valiktor-core:0.12.0")
            }
        }
    }
}

ktlint {
    version.set("0.50.0")
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
