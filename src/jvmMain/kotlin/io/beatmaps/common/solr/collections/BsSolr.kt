package io.beatmaps.common.solr.collections

import io.beatmaps.common.SearchOrder
import io.beatmaps.common.solr.PercentageMinimumMatchExpression
import io.beatmaps.common.solr.SolrCollection
import io.beatmaps.common.solr.SolrScore
import io.beatmaps.common.solr.parsers.EDisMaxQuery
import org.apache.solr.client.solrj.SolrQuery

object BsSolr : SolrCollection() {
    val author = string("author")
    val created = pdate("created")
    val deleted = pdate("deleted")
    val description = string("description")
    override val id = pint("id")
    val mapId = string("mapId")
    val mapper = string("mapper")
    val mapperIds = pints("mapperIds")
    val name = string("name")
    val updated = pdate("updated")
    val curated = pdate("curated")
    val uploaded = pdate("uploaded")
    val voteScore = pfloat("voteScore")
    val verified = boolean("verified")
    val rankedss = boolean("rankedss")
    val rankedbl = boolean("rankedbl")
    val ai = boolean("ai")
    val mapperId = pint("mapperId")
    val curatorId = pint("curatorId")
    val tags = strings("tags")
    val suggestions = strings("suggestions")
    val requirements = strings("requirements")
    val nps = pfloats("nps")
    val fullSpread = boolean("fullSpread")
    val bpm = pfloat("bpm")
    val duration = pint("duration")
    val environment = strings("environment")
    val characteristics = strings("characteristics")
    val upvotes = pint("upvotes")
    val downvotes = pint("downvotes")
    val votes = pint("votes")
    val blStars = pfloats("blStars")
    val ssStars = pfloats("ssStars")

    // Copy fields
    val authorEn = string("author_en")
    val nameEn = string("name_en")
    val descriptionEn = string("description_en")

    // Weights
    private val queryFields = arrayOf(
        name to 4.0,
        nameEn to 1.0,
        author to 10.0,
        authorEn to 2.0,
        descriptionEn to 0.5
    )

    fun newQuery(sortOrder: SearchOrder) =
        EDisMaxQuery()
            .setBoost(if (sortOrder == SearchOrder.Relevance) voteScore else null)
            .setQueryFields(*queryFields)
            .setTie(0.1)
            .setMinimumMatch(
                PercentageMinimumMatchExpression(-0.5f)
            )

    fun addSortArgs(q: SolrQuery, seed: Int?, searchOrder: SearchOrder, ascending: Boolean): SolrQuery =
        when (searchOrder) {
            SearchOrder.Relevance -> listOf(
                SolrScore.sort(ascending)
            )
            SearchOrder.Rating -> listOf(
                voteScore.sort(ascending),
                uploaded.sort(ascending)
            )
            SearchOrder.Latest -> listOf(
                uploaded.sort(ascending)
            )
            SearchOrder.Curated -> listOf(
                curated.sort(ascending),
                uploaded.sort(ascending)
            )
            SearchOrder.Random -> listOf(
                SolrQuery.SortClause("random_$seed", if (ascending) SolrQuery.ORDER.asc else SolrQuery.ORDER.desc)
            )
            SearchOrder.Duration -> listOf(
                duration.sort(ascending),
                uploaded.sort(ascending)
            )
        }.let {
            q.setSorts(it)
        }

    override val collection = System.getenv("SOLR_COLLECTION") ?: "beatsaver"
}
