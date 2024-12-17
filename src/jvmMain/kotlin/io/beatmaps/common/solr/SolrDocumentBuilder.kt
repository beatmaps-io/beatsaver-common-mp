package io.beatmaps.common.solr

import io.beatmaps.common.solr.field.SolrField
import kotlinx.datetime.Instant
import org.apache.solr.common.SolrInputDocument
import org.jetbrains.exposed.dao.id.EntityID

class SolrDocumentBuilder(private val inputDoc: SolrInputDocument) {
    operator fun set(field: SolrField<Instant>, value: java.time.Instant?) =
        inputDoc.setField(field.name, value?.toString())

    operator fun <T> set(field: SolrField<T>, value: T?) =
        inputDoc.setField(field.name, if (value is Instant) value.toString() else value)

    operator fun <T, U : EntityID<T>?> set(field: SolrField<T>, value: U) =
        inputDoc.setField(field.name, value?.value)

    fun <T> update(field: SolrField<T>, value: T?) {
        val partialUpdate = mutableMapOf<String, T?>()
        partialUpdate["set"] = value
        inputDoc.addField(field.name, partialUpdate)
    }
}
