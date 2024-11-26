package io.beatmaps.common.solr

import kotlin.math.ceil

data class SolrResults(val mapIds: List<Int>, val qTime: Int, val numRecords: Int) {
    val pages = ceil(numRecords / 20f).toInt()
    val order = mapIds.mapIndexed { idx, i -> i to idx }.toMap()
    val searchInfo = SearchInfo(numRecords, pages, qTime / 1000f)

    companion object {
        val empty = SolrResults(listOf(), 0, 0)
    }
}
