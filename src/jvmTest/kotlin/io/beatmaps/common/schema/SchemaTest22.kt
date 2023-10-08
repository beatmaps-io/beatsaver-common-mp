package io.beatmaps.common.schema

import io.beatmaps.common.beatsaber.CutDirection
import io.beatmaps.common.schema.SchemaCommon.validateFolder
import io.beatmaps.common.schema.SchemaCommon.violation
import org.junit.Test
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

        assertEquals(16, ex.constraintViolations.size)
        assertEquals(
            setOf(
                violation("_notes[0]._type", 50, In(setOf(0, 1, 3))),
                violation("_notes[0]._cutDirection", 50, CutDirection),
                violation("_notes[0]._time", 100f, Between(0.0f, 64f)),
                violation("_notes[1]._type", null, In(setOf(0, 1, 3))),
                violation("_notes[1]._cutDirection", null, NotNull),
                violation("_notes[1]._time", null, NotNull),
                violation("_notes[1]._lineIndex", null, NotNull),
                violation("_notes[1]._lineLayer", null, NotNull),
                violation("_obstacles[1]._type", null, NotNull),
                violation("_obstacles[1]._duration", null, NotNull),
                violation("_obstacles[1]._time", null, NotNull),
                violation("_obstacles[1]._lineIndex", null, NotNull),
                violation("_obstacles[1]._width", null, NotNull),
                violation("_events[1]._time", null, NotNull),
                violation("_events[1]._type", null, NotNull),
                violation("_events[1]._value", null, NotNull)
            ),
            ex.constraintViolations
        )
    }
}
