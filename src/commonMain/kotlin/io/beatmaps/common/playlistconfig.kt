package io.beatmaps.common

import io.beatmaps.common.api.RankedFilter
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
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
    val ascending: Boolean? = null,
    val from: Instant? = null,
    val to: Instant? = null,
    val noodle: Boolean? = null,
    val ranked: RankedFilter = RankedFilter.All,
    val curated: Boolean? = null,
    val verified: Boolean? = null,
    val fullSpread: Boolean? = null,
    val me: Boolean? = null,
    val cinema: Boolean? = null,
    val vivify: Boolean? = null,
    val tags: MapTagSet = mapOf(),
    val mappers: List<Int> = listOf(),
    val environments: EnvironmentSet = emptySet()
)
