package io.beatmaps.common.schema

import io.beatmaps.common.OptionalProperty
import io.beatmaps.common.beatsaber.NodeNotPresent
import io.beatmaps.common.schema.SchemaCommon.validateFolder
import org.junit.Test
import org.valiktor.DefaultConstraintViolation
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SchemaTest32 {
    @Test
    fun schema3_2() {
        val ex = validateFolder("3_2")
        assertNull(ex)
    }

    @Test
    fun schema3_2as3_0() {
        val ex = validateFolder("3_2as3_0")
        assertNotNull(ex)

        assertEquals(10, ex.constraintViolations.size)
        assertEquals(
            setOf(
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.chunks",
                    OptionalProperty.Present(0),
                    NodeNotPresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.randomType",
                    OptionalProperty.Present(0),
                    NodeNotPresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.seed",
                    OptionalProperty.Present(0),
                    NodeNotPresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.limit",
                    OptionalProperty.Present(0.0f),
                    NodeNotPresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.alsoAffectsType",
                    OptionalProperty.Present(0),
                    NodeNotPresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.chunks",
                    OptionalProperty.Present(0),
                    NodeNotPresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.randomType",
                    OptionalProperty.Present(0),
                    NodeNotPresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.seed",
                    OptionalProperty.Present(0),
                    NodeNotPresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.limit",
                    OptionalProperty.Present(0.0f),
                    NodeNotPresent
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`.lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.alsoAffectsType",
                    OptionalProperty.Present(0),
                    NodeNotPresent
                )
            ),
            ex.constraintViolations
        )
    }
}
