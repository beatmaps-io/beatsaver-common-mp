package io.beatmaps.common

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

sealed interface IPlaylistConfig

@Serializable
@SerialName("Search")
data class SearchPlaylistConfig(val searchParams: SearchParamsPlaylist, val mapCount: Int) : IPlaylistConfig {
    companion object {
        val DEFAULT = SearchPlaylistConfig(SearchParamsPlaylist("", sortOrder = SearchOrder.Relevance, tags = mapOf()), 0)
    }
}

@Serializable
data class SearchParamsPlaylist(
    val search: String = "",
    val automapper: Boolean? = null,
    val minNps: Float? = null,
    val maxNps: Float? = null,
    val chroma: Boolean? = null,
    val sortOrder: SearchOrder = SearchOrder.Relevance,
    val from: Instant? = null,
    val to: Instant? = null,
    val noodle: Boolean? = null,
    val ranked: Boolean? = null,
    val curated: Boolean? = null,
    val verified: Boolean? = null,
    val fullSpread: Boolean? = null,
    val me: Boolean? = null,
    val cinema: Boolean? = null,
    val tags: MapTags = mapOf(),
    val mappers: List<Int> = listOf()
)

fun SerializersModuleBuilder.playlist() = polymorphic(IPlaylistConfig::class) {
    subclass(SearchPlaylistConfig::class)
}
