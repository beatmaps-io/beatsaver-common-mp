package io.beatmaps.common

object Config {
    fun cdnBase(cdnPrefix: String) = "https://${cdnPrefix}cdn.beatsaver.com"

    const val basename: String = "https://beatsaver.com"
    const val cdnbase: String = "https://cdn.beatsaver.com"
    const val apibase: String = "/api"
    const val apiremotebase: String = "https://api.beatsaver.com"
    const val oneClick: Boolean = true
}
