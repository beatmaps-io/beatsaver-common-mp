package io.beatmaps.common

object Config {
    private val baseName = System.getenv("BASE_URL") ?: "http://localhost:8080"
    private val apiBase = System.getenv("BASE_API_URL") ?: "http://localhost:8080/api"

    private val remoteCdn = System.getenv("REMOTE_CDN").let {
        when (it) {
            "false", null -> false
            else -> true
        }
    }

    fun cdnBase(prefix: String, absolute: Boolean = false) = when {
        remoteCdn -> "https://${prefix}cdn.beatsaver.com"
        absolute -> "$baseName/cdn"
        else -> "/cdn"
    }

    fun apiBase(absolute: Boolean = false) = when (absolute) {
        true -> apiBase
        else -> "/api"
    }
    fun siteBase(absolute: Boolean = true) = when (absolute) {
        true -> baseName
        else -> ""
    }
}
