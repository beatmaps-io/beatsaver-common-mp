package io.beatmaps.common

import kotlin.test.Test
import kotlin.test.assertEquals

internal class MapTagTest {
    @Test
    fun mapTagToQueryTest() {
        val tags: MapTagSet = mapOf(
            true to setOf(MapTag.Balanced, MapTag.Anime, MapTag.Metal)
        )

        val query = tags.asQuery()
        val str = query.toQuery()

        assertEquals("balanced,anime|metal", str)
    }

    @Test
    fun mapQueryToSetTest() {
        val tags: MapTagQuery = listOf(
            listOf(
                false to MapTag.Anime,
                true to MapTag.Metal,
                true to MapTag.Classical
            ),
            listOf(
                false to MapTag.Balanced,
                true to MapTag.Challenge
            )
        )

        val set = tags.toTagSet()
        val expected: MapTagSet = mapOf(
            true to setOf(MapTag.Metal, MapTag.Classical, MapTag.Challenge),
            false to setOf(MapTag.Anime, MapTag.Balanced)
        )

        assertEquals(expected, set)

        val query = set.asQuery()
        assertEquals(tags, query)
    }

    @Test
    fun mapTagQueryTest() {
        val tags: MapTagQuery = listOf(
            listOf(
                false to MapTag.Anime,
                true to MapTag.Metal,
                true to MapTag.Classical
            ),
            listOf(
                true to MapTag.Challenge,
                false to MapTag.Balanced
            )
        )

        val str = tags.human()

        assertEquals("!anime and (metal or classical-orchestral) and challenge and !balanced", str)
    }
}
