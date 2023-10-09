package io.beatmaps.common.schema

import io.beatmaps.common.beatsaber.CutDirection
import io.beatmaps.common.schema.SchemaCommon.validateFolder
import io.beatmaps.common.schema.SchemaCommon.violation
import io.beatmaps.common.schema.SchemaCommon.violationWrong
import org.junit.Test
import org.valiktor.constraints.Between
import org.valiktor.constraints.In
import org.valiktor.constraints.Matches
import org.valiktor.constraints.NotNull
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SchemaTest22 {
    @Test
    fun basic() {
        val ex = validateFolder("2_2/basic")
        assertNull(ex)
    }

    @Test
    fun schema() {
        val ex = validateFolder("2_2/default")
        assertNull(ex)
    }

    @Test
    fun error() {
        val ex = validateFolder("2_2/error")
        assertNotNull(ex)

        assertEquals(
            setOf(
                violation("version", "3", Matches(Regex("\\d+\\.\\d+\\.\\d+"))),
                violation("_notes[0]._type", 50, In(setOf(0, 1, 3))),
                violation("_notes[0]._cutDirection", 50, CutDirection),
                violation("_notes[0]._time", 100f, Between(0.0f, 64f)),
                violation("_notes[1]._type", null, In(setOf(0, 1, 3))),
                violation("_notes[1]._cutDirection", null, NotNull),
                violation("_notes[1]._time", null, NotNull),
                violation("_notes[1]._lineIndex", null, NotNull),
                violation("_notes[1]._lineLayer", null, NotNull),
                violationWrong("_notes[2]._type"),
                violationWrong("_notes[2]._cutDirection"),
                violationWrong("_notes[2]._time"),
                violationWrong("_notes[2]._lineIndex"),
                violationWrong("_notes[2]._lineLayer"),
                violation("_notes[3]._type"),
                violation("_notes[3]._cutDirection"),
                violation("_notes[3]._time"),
                violation("_notes[3]._lineIndex"),
                violation("_notes[3]._lineLayer"),
                violationWrong("_notes[4]"),
                violation("_obstacles[1]._type", null, NotNull),
                violation("_obstacles[1]._duration", null, NotNull),
                violation("_obstacles[1]._time", null, NotNull),
                violation("_obstacles[1]._lineIndex", null, NotNull),
                violation("_obstacles[1]._width", null, NotNull),
                violationWrong("_obstacles[2]._type"),
                violationWrong("_obstacles[2]._duration"),
                violationWrong("_obstacles[2]._time"),
                violationWrong("_obstacles[2]._lineIndex"),
                violationWrong("_obstacles[2]._width"),
                violation("_obstacles[3]._type"),
                violation("_obstacles[3]._duration"),
                violation("_obstacles[3]._time"),
                violation("_obstacles[3]._lineIndex"),
                violation("_obstacles[3]._width"),
                violationWrong("_obstacles[4]"),
                violation("_events[1]._time", null, NotNull),
                violation("_events[1]._type", null, NotNull),
                violation("_events[1]._value", null, NotNull),
                violationWrong("_events[2]._time"),
                violationWrong("_events[2]._type"),
                violationWrong("_events[2]._value"),
                violation("_events[3]._time"),
                violation("_events[3]._type"),
                violation("_events[3]._value"),
                violationWrong("_events[4]")
            ),
            ex.constraintViolations
        )
    }

    @Test
    fun badtypes() {
        val ex = validateFolder("2_2/badtypes")
        assertNotNull(ex)

        assertEquals(
            setOf(
                violationWrong("version"),
                violationWrong("_notes"),
                violationWrong("_obstacles"),
                violationWrong("_events"),
                violationWrong("_waypoints"),
                violationWrong("_specialEventsKeywordFilters"),
                violationWrong("_customData"),
                violationWrong("_BPMChanges")
            ),
            ex.constraintViolations
        )
    }

    @Test
    fun missing() {
        val ex = validateFolder("2_2/missing")
        assertNotNull(ex)

        assertEquals(
            setOf(
                violation("version"),
                violation("_notes"),
                violation("_obstacles"),
                violation("_events")
            ),
            ex.constraintViolations
        )
    }

    @Test
    fun `null`() {
        val ex = validateFolder("2_2/null")
        assertNotNull(ex)

        assertEquals(
            setOf(
                violation("version", null, NotNull),
                violation("_notes", null, NotNull),
                violation("_obstacles", null, NotNull),
                violation("_events", null, NotNull),
                violation("_waypoints", null, NotNull),
                violation("_specialEventsKeywordFilters", null, NotNull),
                violation("_customData", null, NotNull),
                violation("_BPMChanges", null, NotNull)
            ),
            ex.constraintViolations
        )
    }
}
