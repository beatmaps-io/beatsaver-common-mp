package io.beatmaps.common

object Config {
    private val baseName = System.getenv("BASE_URL") ?: "https://beatsaver.com"
    private val apiBase = System.getenv("BASE_API_URL") ?: "https://api.beatsaver.com"

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
