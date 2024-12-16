package io.beatmaps.common.solr

import io.beatmaps.common.solr.field.SolrField
import kotlinx.datetime.Instant
import org.apache.solr.client.solrj.SolrQuery

class SolrProduct<T : Number>(private vararg val args: SolrFunction<T>) : SolrFunction<T>() {
    override fun toText() = "product(${args.joinToString(",") { it.toText() }})"
}

/**
 * recip(x,m,a,b) implementing a/(m*x+b)
 * where m,a,b are constants, and x is any arbitrarily complex function
 */
class SolrRecip<T : Number, U : Number, V : Number, W : Number>(private val x: SolrFunction<T>, private val m: U, private val a: V, private val b: W) : SolrFunction<Float>() {
    override fun toText() = "recip(${x.toText()},$m,$a,$b)"
}

class SolrOrd(private val field: SolrField<*>) : SolrFunction<Int>() {
    override fun toText() = "ord(${field.toText()})"
}

class SolrRord(private val field: SolrField<*>) : SolrFunction<Int>() {
    override fun toText() = "rord(${field.toText()})"
}

class SolrScale<T : Number>(private val field: SolrField<*>, private val min: T, private val max: T) : SolrFunction<T>() {
    override fun toText() = "scale(${field.toText()},$min,$max)"
}

class SolrDiv<T: Number>(private vararg val args: SolrFunction<T>) : SolrFunction<Float>() {
    override fun toText() = "div(${args.joinToString(",") { it.toText() }})"
}

class SolrSum<T: Number>(private vararg val args: SolrFunction<T>) : SolrFunction<T>() {
    override fun toText() = "sum(${args.joinToString(",") { it.toText() }})"
}

class SolrConstant<T: Number>(private val a: T) : SolrFunction<T>() {
    override fun toText() = "$a"
}

fun <T: Number> T.solr() = SolrConstant(this)

class SolrMs(private val a: SolrFunction<Instant>? = null, private val b: SolrFunction<Instant>? = null) : SolrFunction<Long>() {
    override fun toText() = listOfNotNull(a, b).joinToString(",", "ms(", ")") { it.toText() }
}

object SolrNow : SolrFunction<Instant>() {
    override fun toText() = "NOW"
}

class SolrInstant(private val t: Instant) : SolrFunction<Instant>() {
    override fun toText() = t.toString()
}

object SolrBaseScore : SolrFunction<Float>() {
    override fun toText() = "query(\$q)"
}

abstract class SolrFunction<T> {
    abstract fun toText(): String

    fun sort(order: SolrQuery.ORDER) = SolrQuery.SortClause(toText(), order)
    fun asc() = sort(SolrQuery.ORDER.asc)
    fun desc() = sort(SolrQuery.ORDER.desc)
}

object SolrScore : SolrFunction<Float>() {
    override fun toText() = "score"
}
