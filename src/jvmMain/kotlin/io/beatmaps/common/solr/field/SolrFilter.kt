package io.beatmaps.common.solr.field

import org.apache.solr.client.solrj.SolrQuery

interface SolrFilter {
    fun toText(): String

    infix fun and(other: SolrFilter): SolrFilter
    infix fun or(other: SolrFilter): SolrFilter
    fun not(): SolrFilter
}

data class SimpleFilter(val field: String, val value: String, val canQuote: Boolean = false) : SolrFilter {
    override fun toText() = if (canQuote && value.contains(' ')) "$field:\"$value\"" else "$field:$value"
    override fun and(other: SolrFilter) = CompoundFilter("${toText()} AND ${other.toText()}")
    override fun or(other: SolrFilter) = CompoundFilter("${toText()} OR ${other.toText()}")
    override fun not() = CompoundFilter("NOT ${toText()}")
}

data class CompoundFilter(val filter: String) : SolrFilter {
    override fun toText() = filter
    override fun and(other: SolrFilter) = CompoundFilter("$filter AND ${other.toText()}")
    override fun or(other: SolrFilter) = CompoundFilter("$filter OR ${other.toText()}")
    override fun not() = CompoundFilter("NOT ($filter)")
}

@JvmName("applyOptional")
fun SolrQuery.apply(filter: SolrFilter?): SolrQuery =
    if (filter == null) this else apply(filter)

fun SolrQuery.apply(filter: SolrFilter): SolrQuery =
    addFilterQuery(filter.toText())
