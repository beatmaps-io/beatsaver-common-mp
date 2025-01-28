package io.beatmaps.common

import io.beatmaps.common.dbo.DifficultyDao
import io.beatmaps.common.solr.collections.BsSolr
import io.beatmaps.common.solr.field.eq

object ModChecker {
    private const val CHROMA = "Chroma"
    private const val NOODLE = "Noodle Extensions"
    private const val ME = "Mapping Extensions"
    private const val CINEMA = "Cinema"
    private const val VIVIFY = "Vivify"

    private fun List<String>?.containsIgnoreCase(element: String) = this?.any { e -> e.equals(element, true) } ?: false
    private fun checkDiff(diff: DifficultyDao, modName: String, allowAsSuggestion: Boolean = false) =
        diff.requirements.containsIgnoreCase(modName) || (allowAsSuggestion && diff.suggestions.containsIgnoreCase(modName))

    fun chroma(diff: DifficultyDao) = checkDiff(diff, CHROMA, true)
    fun me(diff: DifficultyDao) = checkDiff(diff, ME)
    fun ne(diff: DifficultyDao) = checkDiff(diff, NOODLE)
    fun cinema(diff: DifficultyDao) = checkDiff(diff, CINEMA, true)
    fun vivify(diff: DifficultyDao) = checkDiff(diff, VIVIFY)

    private fun solrQuery(modName: String, allowAsSuggestion: Boolean = false) =
        (BsSolr.requirements eq modName).let {
            if (allowAsSuggestion) {
                it or (BsSolr.suggestions eq modName)
            } else {
                it
            }
        }

    fun chroma() = solrQuery(CHROMA, true)
    fun me() = solrQuery(ME)
    fun ne() = solrQuery(NOODLE)
    fun cinema() = solrQuery(CINEMA, true)
    fun vivify() = solrQuery(VIVIFY)
}
