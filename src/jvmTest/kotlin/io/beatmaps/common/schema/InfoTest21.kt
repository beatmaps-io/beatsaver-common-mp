package io.beatmaps.common.schema

import io.beatmaps.common.beatsaber.CorrectType
import io.beatmaps.common.schema.SchemaCommon.infoViolation
import io.beatmaps.common.schema.SchemaCommon.validateFolder
import org.junit.Test
import org.valiktor.constraints.Less
import kotlin.test.assertContentEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class InfoTest21 {
    @Test
    fun basic() {
        val ex = validateFolder("info/2_1/basic")
        assertNull(ex)
    }

    @Test
    fun error() {
        val ex = validateFolder("info/2_1/error")
        assertNotNull(ex)

        assertContentEquals(
            listOf(
                infoViolation<CorrectType>("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._customData._editorOffset"),
                infoViolation<Less<Int>>("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._beatmapColorSchemeIdx"),
                infoViolation<Less<Int>>("_difficultyBeatmapSets[0]._difficultyBeatmaps[0]._environmentNameIdx")
            ),
            ex.constraintViolations
        )
    }
}
