package io.beatmaps.common.solr

import io.beatmaps.common.solr.field.SolrField
import io.beatmaps.common.timeIt
import io.ktor.server.application.ApplicationCall
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.SolrServerException
import org.apache.solr.client.solrj.impl.BaseHttpSolrClient.RemoteSolrException
import org.apache.solr.client.solrj.impl.Http2SolrClient
import org.apache.solr.common.params.ModifiableSolrParams
import java.util.logging.Level
import java.util.logging.Logger

object SolrHelper {
    private val solrHost = System.getenv("SOLR_HOST") ?: "http://localhost:8983/solr"
    private val solrUser = System.getenv("SOLR_USER") ?: "solr"
    private val solrPass = System.getenv("SOLR_PASS") ?: "insecure-password"
    val enabled = System.getenv("SOLR_ENABLED") == "true"

    val solr: Http2SolrClient by lazy {
        Http2SolrClient.Builder(solrHost)
            .withBasicAuthCredentials(solrUser, solrPass)
            .build()
    }

    val logger: Logger = Logger.getLogger("bmio.SolrHelper")

    const val MS_PER_YEAR = 3.16e-11f
}

fun SolrQuery.all(): SolrQuery =
    setQuery("*:*")

fun SolrQuery.paged(page: Int = 0, pageSize: Int = 20): SolrQuery =
    setFields("id")
        .setStart(page * pageSize).setRows(pageSize)

suspend fun ModifiableSolrParams.getIds(coll: SolrCollection, field: SolrField<Int>? = null, call: ApplicationCall? = null) =
    try {
        val fieldName = field?.name ?: "id"
        val response = call.timeIt("search") {
            coll.query(this)
        }

        val mapIds = response.results.mapNotNull { it[fieldName] as? Int }
        val numRecords = response.results.numFound.toInt()

        SolrResults(mapIds, response.qTime, numRecords)
    } catch (e: RemoteSolrException) {
        SolrHelper.logger.log(Level.WARNING, "Failed to perform solr query $this", e)
        SolrResults.empty
    } catch (e: SolrServerException) {
        SolrHelper.logger.log(Level.WARNING, "Failed to perform solr query $this", e)
        SolrResults.empty
    }
