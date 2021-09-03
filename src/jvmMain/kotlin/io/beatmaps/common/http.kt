package io.beatmaps.common

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.apache.Apache
import io.ktor.client.engine.apache.ApacheEngineConfig
import io.ktor.client.features.HttpTimeout
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import java.net.Inet4Address

private fun setupClient(block: HttpClientConfig<ApacheEngineConfig>.() -> Unit = {}) = HttpClient(Apache) {
    install(HttpTimeout)
    install(JsonFeature) {
        serializer = JacksonSerializer(jackson)
    }

    engine {
        customizeClient {
            setMaxConnTotal(100)
            setMaxConnPerRoute(20)
        }
    }

    block()
}

val client = setupClient()
val localIps = (System.getenv("LOCAL_IPS") ?: "").split(",").filter { it.isNotEmpty() }.map { Inet4Address.getByName(it) }

val randomClient = setupClient {
    engine {
        customizeRequest {
            if (localIps.isNotEmpty()) {
                setLocalAddress(localIps.random())
            }
        }
    }
}
