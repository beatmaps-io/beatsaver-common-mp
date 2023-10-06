package io.beatmaps.common.schema

import io.beatmaps.common.OptionalProperty
import io.beatmaps.common.beatsaber.CutDirection
import io.beatmaps.common.schema.SchemaCommon.validateFolder
import org.junit.Test
import org.valiktor.DefaultConstraintViolation
import org.valiktor.constraints.Between
import org.valiktor.constraints.In
import org.valiktor.constraints.NotNull
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SchemaTest22 {
    @Test
    fun basic2_2() {
        val ex = validateFolder("basic2_2")

        ex?.constraintViolations?.forEach {
            println(it)
        }

        assertNull(ex)
    }

    @Test
    fun schema2_2() {
        val ex = validateFolder("2_2")
        assertNull(ex)
    }

    @Test
    fun error2_2() {
        val ex = validateFolder("error2_2")
        assertNotNull(ex)

        ex.constraintViolations.forEach {
            println(it)
        }

        assertEquals(16, ex.constraintViolations.size)
        assertEquals(
            setOf(
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`._notes[0]._type",
                    OptionalProperty.Present(50),
                    In(setOf(0, 1, 3))
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`._notes[0]._cutDirection",
                    OptionalProperty.Present(50),
                    CutDirection
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`._notes[0]._time",
                    OptionalProperty.Present(100.0f),
                    Between(0.0f, 64f)
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`._notes[1]._type",
                    OptionalProperty.Present<Int?>(null),
                    In(setOf(0, 1, 3))
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`._notes[1]._cutDirection",
                    OptionalProperty.Present<Int?>(null),
                    NotNull
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`._notes[1]._time",
                    OptionalProperty.Present<Float?>(null),
                    NotNull
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`._notes[1]._lineIndex",
                    OptionalProperty.Present<Int?>(null),
                    NotNull
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`._notes[1]._lineLayer",
                    OptionalProperty.Present<Int?>(null),
                    NotNull
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`._obstacles[1]._type",
                    OptionalProperty.Present<Int?>(null),
                    NotNull
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`._obstacles[1]._duration",
                    OptionalProperty.Present<Float?>(null),
                    NotNull
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`._obstacles[1]._time",
                    OptionalProperty.Present<Float?>(null),
                    NotNull
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`._obstacles[1]._lineIndex",
                    OptionalProperty.Present<Int?>(null),
                    NotNull
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`._obstacles[1]._width",
                    OptionalProperty.Present<Int?>(null),
                    NotNull
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`._events[1]._time",
                    OptionalProperty.Present<Float?>(null),
                    NotNull
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`._events[1]._type",
                    OptionalProperty.Present<Int?>(null),
                    NotNull
                ),
                DefaultConstraintViolation(
                    "_difficultyBeatmapSets[0]._difficultyBeatmaps[0].`Easy.dat`._events[1]._value",
                    OptionalProperty.Present<Int?>(null),
                    NotNull
                )
            ),
            ex.constraintViolations
        )
    }
}
