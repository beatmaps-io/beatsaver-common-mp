package io.beatmaps.common

import io.beatmaps.common.util.TextHelper
import kotlin.test.Test
import kotlin.test.assertEquals

class TextHelperTest {
    @Test
    fun shortenTextTest() {
        val originalText = "The quick brown fox jumps over the lazy dog"

        val cases = mapOf(
            1 to "...",
            3 to "...",
            4 to "T...",
            5 to "Th...",
            6 to "The...",
            10 to "The...",
            11 to "The quick...",
            16 to "The quick...",
            17 to "The quick brown...",
            20 to "The quick brown...",
            21 to "The quick brown fox...",
            26 to "The quick brown fox...",
            27 to "The quick brown fox jumps...",
            100 to originalText
        )

        cases.forEach { (max, expected) ->
            val shortText = TextHelper.ellipsize(originalText, max)
            assertEquals(expected, shortText)
        }
    }
}
