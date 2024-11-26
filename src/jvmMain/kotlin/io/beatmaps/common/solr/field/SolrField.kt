package io.beatmaps.common.solr.field

import io.beatmaps.common.solr.SolrCollection
import io.beatmaps.common.solr.SolrFunction

data class SolrField<T>(private val collection: SolrCollection, val name: String) : SolrFunction<T>() {
    override fun toText() = name

    infix fun lessEq(value: T) = lessThanEq(this, "$value")
    infix fun greaterEq(value: T) = greaterThanEq(this, "$value")
    infix fun less(value: T) = lessThan(this, "$value")
    infix fun greater(value: T) = greaterThan(this, "$value")
    infix fun eq(value: T) = eq(this, "$value")
    fun any() = eq(this, "*")
}

private fun lessThanEq(field: SolrField<*>, value: String) =
    SimpleFilter(field.name, "[* TO $value]")

private fun greaterThanEq(field: SolrField<*>, value: String) =
    SimpleFilter(field.name, "[$value TO *]")

private fun lessThan(field: SolrField<*>, value: String) =
    SimpleFilter(field.name, "{* TO $value}")

private fun greaterThan(field: SolrField<*>, value: String) =
    SimpleFilter(field.name, "{$value TO *}")

private fun eq(field: SolrField<*>, value: String) =
    SimpleFilter(field.name, value, true)

infix fun <T> SolrField<List<T>>.lessEq(value: T) = lessThanEq(this, "$value")
infix fun <T> SolrField<List<T>>.greaterEq(value: T) = greaterThanEq(this, "$value")
infix fun <T> SolrField<List<T>>.less(value: T) = lessThan(this, "$value")
infix fun <T> SolrField<List<T>>.greater(value: T) = greaterThan(this, "$value")
infix fun <T> SolrField<List<T>>.eq(value: T) = eq(this, "$value")
