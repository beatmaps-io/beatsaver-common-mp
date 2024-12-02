package io.beatmaps.common.solr

import com.github.michaelbull.retry.policy.constantDelay
import com.github.michaelbull.retry.policy.continueIf
import com.github.michaelbull.retry.policy.plus
import com.github.michaelbull.retry.policy.stopAtAttempts
import com.github.michaelbull.retry.retry
import io.beatmaps.common.solr.field.SolrField
import kotlinx.datetime.Instant
import org.apache.solr.client.solrj.SolrRequest
import org.apache.solr.client.solrj.SolrServerException
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.common.SolrInputDocument
import org.apache.solr.common.params.ModifiableSolrParams

abstract class SolrCollection {
    private val _fields = mutableListOf<SolrField<*>>()
    abstract val collection: String
    open val retryPolicy = continueIf<Throwable> { (failure) -> failure is SolrServerException } + constantDelay(10) + stopAtAttempts(2)

    // Built in scoring for each result
    val score = pfloat("score")

    fun string(name: String): SolrField<String> = registerField(name)
    fun strings(name: String): SolrField<List<String>> = registerField(name)
    fun pdate(name: String): SolrField<Instant> = registerField(name)
    fun pint(name: String): SolrField<Int> = registerField(name)
    fun pints(name: String): SolrField<List<Int>> = registerField(name)
    fun pfloat(name: String): SolrField<Float> = registerField(name)
    fun pfloats(name: String): SolrField<List<Float>> = registerField(name)
    fun boolean(name: String): SolrField<Boolean> = registerField(name)

    private fun <T> registerField(name: String) = SolrField<T>(this, name).also { _fields.add(it) }

    suspend fun query(params: ModifiableSolrParams): QueryResponse = retry(retryPolicy) {
        SolrHelper.solr.query(collection, params, SolrRequest.METHOD.POST)
    }

    internal fun add(doc: SolrInputDocument) {
        SolrHelper.solr.add(collection, doc)
    }

    internal fun add(docs: List<SolrInputDocument>) {
        SolrHelper.solr.add(collection, docs)
    }

    fun delete(id: String) {
        SolrHelper.solr.deleteById(collection, id)
    }
}

fun <T : SolrCollection> T.insert(block: T.(SolrDocumentBuilder) -> Unit) {
    val inputDoc = SolrInputDocument()
    block(this, SolrDocumentBuilder(inputDoc))

    add(inputDoc)
}

fun <T : SolrCollection, U> T.insertMany(input: List<U>, block: T.(SolrDocumentBuilder, U) -> Unit) {
    val inputDocs = input.map { i ->
        SolrInputDocument().also {
            block(this, SolrDocumentBuilder(it), i)
        }
    }

    add(inputDocs)
}
