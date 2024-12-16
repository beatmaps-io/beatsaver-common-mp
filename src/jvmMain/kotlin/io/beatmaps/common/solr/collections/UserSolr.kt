package io.beatmaps.common.solr.collections

import io.beatmaps.common.api.ApiOrder
import io.beatmaps.common.api.UserSearchSort
import io.beatmaps.common.solr.PercentageMinimumMatchExpression
import io.beatmaps.common.solr.SolrCollection
import io.beatmaps.common.solr.SolrDiv
import io.beatmaps.common.solr.SolrScale
import io.beatmaps.common.solr.SolrSum
import io.beatmaps.common.solr.parsers.EDisMaxQuery
import io.beatmaps.common.solr.solr
import org.apache.solr.client.solrj.SolrQuery

object UserSolr : SolrCollection() {
    override val id = pint("id")
    val sId = string("sId")

    val name = string("name")
    val description = string("description")

    val created = pdate("created")

    val admin = boolean("admin")
    val curator = boolean("curator")
    val seniorCurator = boolean("seniorCurator")
    val verifiedMapper = boolean("verifiedMapper")

    val avgBpm = pfloat("avgBpm")
    val avgDuration = pfloat("avgDuration")
    val totalUpvotes = pint("totalUpvotes")
    val totalDownvotes = pint("totalDownvotes")
    val avgScore = pfloat("avgScore")
    val totalMaps = pint("totalMaps")
    val rankedMaps = pint("rankedMaps")

    val firstUpload = pdate("firstUpload").optional()
    val lastUpload = pdate("lastUpload").optional()
    val mapAge = pint("mapAge")

    // Copy fields
    val nameEn = string("name_en")
    val descriptionEn = string("description_en")

    // Weights
    private val queryFields = arrayOf(
        name to 4.0,
        nameEn to 1.0,
        descriptionEn to 0.5
    )

    private val boostFunction = SolrScale(totalMaps, 0f, 1f)

    fun newQuery() =
        EDisMaxQuery()
            .setBoostFunction(boostFunction)
            .setQueryFields(*queryFields)
            .setTie(0.1)
            .setMinimumMatch(
                PercentageMinimumMatchExpression(-0.5f)
            )

    private val ratio = SolrDiv(totalUpvotes, SolrSum(totalUpvotes, totalDownvotes, 1.solr()))

    fun addSortArgs(q: SolrQuery, sort: UserSearchSort, order: ApiOrder): SolrQuery {
        val field = when(sort) {
            UserSearchSort.RELEVANCE -> score
            UserSearchSort.BPM -> avgBpm
            UserSearchSort.DURATION -> avgDuration
            UserSearchSort.UPVOTES -> totalUpvotes
            UserSearchSort.DOWNVOTES -> totalDownvotes
            UserSearchSort.RATIO -> ratio
            UserSearchSort.MAPS -> totalMaps
            UserSearchSort.RANKED_MAPS -> rankedMaps
            UserSearchSort.FIRST_UPLOAD -> firstUpload
            UserSearchSort.LAST_UPLOAD -> lastUpload
            UserSearchSort.MAP_AGE -> mapAge
        }
        val solrOrder = when(order) {
            ApiOrder.DESC -> SolrQuery.ORDER.desc
            ApiOrder.ASC -> SolrQuery.ORDER.asc
        }
        return q.setSorts(listOf(field.sort(solrOrder)))
    }

    override val collection = System.getenv("SOLR_USER_COLLECTION") ?: "users"
}
