package io.beatmaps.common.solr.parsers

import io.beatmaps.common.solr.MinimumMatchExpression
import io.beatmaps.common.solr.field.SolrField
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.common.params.DisMaxParams

open class DisMaxQuery : SolrQuery() {
    init {
        set(DEF_TYPE, DISMAX)
    }

    fun setQueryFields(vararg fields: Pair<SolrField<*>, Double>) = this.also {
        set(DisMaxParams.QF, fields.joinToString(" ") { "${it.first.toText()}^${it.second}" })
    }

    fun setTie(tie: Double) = this.also {
        set(DisMaxParams.TIE, tie.toString())
    }

    fun setMinimumMatch(vararg exp: MinimumMatchExpression) = this.also {
        set(DisMaxParams.MM, exp.joinToString(" ") { it.toText() })
    }

    fun setPhraseFields(vararg fields: Pair<SolrField<*>, Double>) = this.also {
        set(DisMaxParams.PF, fields.joinToString(" ") { "${it.first.toText()}^${it.second}" })
    }

    fun setPhraseSlop(slop: Int) = this.also {
        set(DisMaxParams.PS, slop.toString())
    }

    fun setQueryPhraseSlop(slop: Int) = this.also {
        set(DisMaxParams.QS, slop.toString())
    }

    companion object {
        const val DEF_TYPE = "defType"
        const val DISMAX = "dismax"
    }
}
