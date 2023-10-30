package io.beatmaps.common

enum class SortOrderTarget {
    Map, Playlist;

    companion object {
        val all = values().toList()
    }
}

enum class SearchOrder(val idx: Int, val targets: List<SortOrderTarget>) {
    Latest(0, SortOrderTarget.all),
    Relevance(1, SortOrderTarget.all),
    Rating(2, listOf(SortOrderTarget.Map)),
    Curated(3, SortOrderTarget.all);

    companion object {
        private val map = values().associateBy(SearchOrder::idx)
        fun fromInt(type: Int) = map[type]

        fun fromString(str: String?) = try {
            valueOf(str ?: "")
        } catch (e: Exception) {
            null
        }
    }
}

typealias MapTagSet = Map<Boolean, Set<MapTag>>
typealias MapTags = Map<Boolean, Map<MapTagType, List<String>>>
fun emptyTags(): MapTags = mapOf(true to mapOf(), false to mapOf())
fun MapTagSet.toTags(): MapTags = mapValues { o -> o.value.groupBy { y -> y.type }.mapValues { y -> y.value.map { x -> x.slug } } }.filterValues { o -> o.isNotEmpty() }
fun MapTags.toSet(): MapTagSet = mapValues { o -> o.value.flatMap { x -> x.value.mapNotNull { y -> MapTag.fromSlug(y) } }.toSet() }
fun MapTags.human() = flatMap { x ->
    x.value.map { y ->
        y.value.joinToString(if (x.key) " or " else " and ") {
            (if (x.key) "" else "!") + it
        } to y.value.size
    }
}.joinToString(" and ") { if (it.second == 1) it.first else "(${it.first})" }
fun MapTags.toQuery() = flatMap { x ->
    x.value.map { y ->
        y.value.joinToString(if (x.key) "|" else ",") {
            (if (x.key) "" else "!") + it
        }
    }
}.joinToString(",")
fun String?.toTags(): MapTags? = this?.split(",", "|")?.groupBy { !it.startsWith("!") }?.mapValues {
    it.value.map { slug -> slug.removePrefix("!") }.groupBy { slug -> MapTag.fromSlug(slug)?.type ?: MapTagType.None }
}
