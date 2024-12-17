package io.beatmaps.common.solr.field

import io.beatmaps.common.solr.SolrCollection
import io.beatmaps.common.solr.SolrFunction

data class SolrField<T>(private val collection: SolrCollection, val name: String) : SolrFunction<T>() {
    override fun toText() = name

    fun betweenInc(from: T, to: T) = betweenInc(this, "$from", "$to")
    fun betweenExc(from: T, to: T) = betweenExc(this, "$from", "$to")
    fun betweenNullableInc(from: T?, to: T?) = betweenNullableInc(this, from?.let { "$it" }, to?.let { "$it" })

    infix fun lessEq(value: T) = lessThanEq(this, "$value")
    infix fun greaterEq(value: T) = greaterThanEq(this, "$value")
    infix fun less(value: T) = lessThan(this, "$value")
    infix fun greater(value: T) = greaterThan(this, "$value")
    infix fun eq(value: T) = eq(this, "$value")
    fun any() = eq(this, "*")

    fun optional(): SolrField<T?> = SolrField(collection, name)
    fun <T> coerce() = SolrField<T>(collection, name)
}

private fun betweenNullableInc(field: SolrField<*>, a: String?, b: String?) =
    if (a != null && b != null) {
        betweenInc(field, a, b)
    } else if (a != null) {
        greaterThanEq(field, a)
    } else if (b != null) {
        lessThanEq(field, b)
    } else {
        null
    }

private fun betweenInc(field: SolrField<*>, from: String, to: String) =
    SimpleFilter(field.name, "[$from TO $to]")

private fun betweenExc(field: SolrField<*>, from: String, to: String) =
    SimpleFilter(field.name, "{$from TO $to}")

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

fun <T> SolrField<List<T>>.betweenInc(from: T, to: T) = betweenInc(this, "$from", "$to")
fun <T> SolrField<List<T>>.betweenExc(from: T, to: T) = betweenExc(this, "$from", "$to")
fun <T> SolrField<List<T>>.betweenNullableInc(from: T?, to: T?) = betweenNullableInc(this, from?.let { "$it" }, to?.let { "$it" })

infix fun <T> SolrField<List<T>>.lessEq(value: T) = lessThanEq(this, "$value")
infix fun <T> SolrField<List<T>>.greaterEq(value: T) = greaterThanEq(this, "$value")
infix fun <T> SolrField<List<T>>.less(value: T) = lessThan(this, "$value")
infix fun <T> SolrField<List<T>>.greater(value: T) = greaterThan(this, "$value")
infix fun <T> SolrField<List<T>>.eq(value: T) = eq(this, "$value")
