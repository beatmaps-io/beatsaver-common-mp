package io.beatmaps.common

enum class SortOrderTarget {
    UserMap, Map, Playlist;

    companion object {
        val all = entries
    }
}

enum class SearchOrder(val idx: Int, val targets: List<SortOrderTarget>) {
    Latest(0, SortOrderTarget.all),
    Relevance(1, listOf(SortOrderTarget.Map, SortOrderTarget.Playlist)),
    Rating(2, listOf(SortOrderTarget.UserMap, SortOrderTarget.Map)),
    Curated(3, SortOrderTarget.all),
    Random(4, listOf());

    companion object {
        private val map = entries.associateBy(SearchOrder::idx)
        fun fromInt(type: Int) = map[type]

        fun fromString(str: String?) = try {
            valueOf(str ?: "")
        } catch (e: Exception) {
            null
        }
    }
}

typealias MapTagSet = Map<Boolean, Set<MapTag>>
typealias MapTagQuery = List<List<Pair<Boolean, MapTag>>>

fun MapTagSet.toQuery() = asQuery().toQuery()

fun MapTagSet.asQuery(): MapTagQuery =
    flatMap { x ->
        x.value.map { x.key to it }
    }
        .groupBy { it.second.type }
        .values
        .toList()

fun String?.toQuery(): MapTagQuery? = this?.split(",")?.map { p ->
    p.split("|").mapNotNull { q ->
        MapTag.fromSlug(q.removePrefix("!"))
            ?.let { if (it == MapTag.None) null else it }
            ?.let { m -> !q.startsWith("!") to m }
    }
}

fun MapTagQuery.toQuery() = flatMap { x ->
    x.groupBy { it.first }.map { y ->
        y.value.joinToString(if (y.key) "|" else ",") {
            (if (it.first) "" else "!") + it.second.slug
        }
    }
}.joinToString(",")

fun MapTagQuery.human() = flatMap { x ->
    x.groupBy { it.first }.map { y ->
        y.value.joinToString(if (y.key) " or " else " and ") {
            (if (it.first) "" else "!") + it.second.slug
        } to y.value.size
    }
}.joinToString(" and ") { if (it.second == 1) it.first else "(${it.first})" }

fun MapTagQuery.toTagSet(): MapTagSet =
    flatten()
        .groupBy { it.first }
        .mapValues { x ->
            x.value.map { it.second }.toSet()
        }
