package io.beatmaps.common

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.apache.Apache
import io.ktor.client.engine.apache.ApacheEngineConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.jackson.JacksonConverter
import io.ktor.serialization.kotlinx.json.json

private fun setupClient(block: HttpClientConfig<ApacheEngineConfig>.() -> Unit = {}) = HttpClient(Apache) {
    install(HttpTimeout)

    block()

    engine {
        customizeClient {
            setMaxConnTotal(100)
            setMaxConnPerRoute(20)
        }
    }
}

val client = setupClient {
    install(ContentNegotiation) {
        val converter = JacksonConverter(jackson)
        register(ContentType.Application.Json, converter)
    }
}
val jsonClient = setupClient {
    install(ContentNegotiation) {
        json(jsonIgnoreUnknown)
    }
}
