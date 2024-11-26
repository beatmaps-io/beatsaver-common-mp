package io.beatmaps.common.solr

import kotlinx.serialization.Serializable

@Serializable
data class SearchInfo(val total: Int, val pages: Int, val duration: Float)
