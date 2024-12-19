package io.beatmaps.common.solr.field

import io.beatmaps.common.solr.SolrFunction
import org.apache.solr.client.solrj.SolrQuery

interface SolrFilter {
    fun toText(): String
}

interface ComposableSolrFilter : SolrFilter {
    infix fun and(other: ComposableSolrFilter): ComposableSolrFilter
    infix fun or(other: ComposableSolrFilter): ComposableSolrFilter
    fun not(): ComposableSolrFilter
}

data class FRangeFilter<T>(val func: SolrFunction<T>, val lower: T? = null, val upper: T? = null, val inclusiveLower: Boolean? = null, val inclusiveUpper: Boolean? = null) : SolrFilter {
    private val props = mapOf(
        "u" to upper,
        "l" to lower,
        "incl" to inclusiveLower,
        "incu" to inclusiveUpper
    ).filterValues { it != null }.map { "${it.key}=${it.value}" }.joinToString(" ")

    override fun toText() = "{!frange $props}${func.toText()}"
}

data class SimpleFilter(val field: String, val value: String, val canQuote: Boolean = false) : ComposableSolrFilter {
    override fun toText() = if (canQuote && value.contains(' ')) "$field:\"$value\"" else "$field:$value"
    override fun and(other: ComposableSolrFilter) = CompoundFilter("${toText()} AND ${other.toText()}")
    override fun or(other: ComposableSolrFilter) = CompoundFilter("${toText()} OR ${other.toText()}")
    override fun not() = CompoundFilter("NOT ${toText()}")
}

data class CompoundFilter(val filter: String) : ComposableSolrFilter {
    override fun toText() = filter
    override fun and(other: ComposableSolrFilter) = CompoundFilter("$filter AND ${other.toText()}")
    override fun or(other: ComposableSolrFilter) = CompoundFilter("$filter OR ${other.toText()}")
    override fun not() = CompoundFilter("NOT ($filter)")
}

@JvmName("applyOptional")
fun SolrQuery.apply(filter: SolrFilter?): SolrQuery =
    if (filter == null) this else apply(filter)

fun SolrQuery.apply(filter: SolrFilter): SolrQuery =
    addFilterQuery(filter.toText())
