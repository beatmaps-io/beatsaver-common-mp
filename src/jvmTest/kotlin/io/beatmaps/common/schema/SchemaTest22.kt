package io.beatmaps.common.schema

import io.beatmaps.common.beatsaber.CorrectType
import io.beatmaps.common.beatsaber.CutDirection
import io.beatmaps.common.beatsaber.NodePresent
import io.beatmaps.common.schema.SchemaCommon.validateFolder
import io.beatmaps.common.schema.SchemaCommon.violation
import org.junit.Test
import org.valiktor.constraints.Between
import org.valiktor.constraints.In
import org.valiktor.constraints.Matches
import org.valiktor.constraints.NotNull
import kotlin.test.assertContentEquals
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

        assertContentEquals(
            listOf<Any>(
                violation<Matches>("version"),
                violation<In<Int>>("_notes[0]._type"),
                violation<CutDirection>("_notes[0]._cutDirection"),
                violation<Between<Float>>("_notes[0]._time"),
                violation<In<Int>>("_notes[1]._type"),
                violation<NotNull>("_notes[1]._cutDirection"),
                violation<NotNull>("_notes[1]._time"),
                violation<NotNull>("_notes[1]._lineIndex"),
                violation<NotNull>("_notes[1]._lineLayer"),
                violation<CorrectType>("_notes[2]._type"),
                violation<CorrectType>("_notes[2]._cutDirection"),
                violation<CorrectType>("_notes[2]._time"),
                violation<CorrectType>("_notes[2]._lineIndex"),
                violation<CorrectType>("_notes[2]._lineLayer"),
                violation<NodePresent>("_notes[3]._type"),
                violation<NodePresent>("_notes[3]._cutDirection"),
                violation<NodePresent>("_notes[3]._time"),
                violation<NodePresent>("_notes[3]._lineIndex"),
                violation<NodePresent>("_notes[3]._lineLayer"),
                violation<CorrectType>("_notes[4]"),
                violation<NotNull>("_obstacles[1]._type"),
                violation<NotNull>("_obstacles[1]._duration"),
                violation<NotNull>("_obstacles[1]._time"),
                violation<NotNull>("_obstacles[1]._lineIndex"),
                violation<NotNull>("_obstacles[1]._width"),
                violation<CorrectType>("_obstacles[2]._type"),
                violation<CorrectType>("_obstacles[2]._duration"),
                violation<CorrectType>("_obstacles[2]._time"),
                violation<CorrectType>("_obstacles[2]._lineIndex"),
                violation<CorrectType>("_obstacles[2]._width"),
                violation<NodePresent>("_obstacles[3]._type"),
                violation<NodePresent>("_obstacles[3]._duration"),
                violation<NodePresent>("_obstacles[3]._time"),
                violation<NodePresent>("_obstacles[3]._lineIndex"),
                violation<NodePresent>("_obstacles[3]._width"),
                violation<CorrectType>("_obstacles[4]"),
                violation<NotNull>("_events[1]._time"),
                violation<NotNull>("_events[1]._type"),
                violation<NotNull>("_events[1]._value"),
                violation<CorrectType>("_events[2]._time"),
                violation<CorrectType>("_events[2]._type"),
                violation<CorrectType>("_events[2]._value"),
                violation<NodePresent>("_events[3]._time"),
                violation<NodePresent>("_events[3]._type"),
                violation<NodePresent>("_events[3]._value"),
                violation<CorrectType>("_events[4]")
            ),
            ex.constraintViolations
        )
    }

    @Test
    fun badtypes() {
        val ex = validateFolder("2_2/badtypes")
        assertNotNull(ex)

        assertContentEquals(
            listOf<Any>(
                violation<CorrectType>("version"),
                violation<CorrectType>("_notes"),
                violation<CorrectType>("_obstacles"),
                violation<CorrectType>("_events"),
                violation<CorrectType>("_waypoints"),
                violation<CorrectType>("_specialEventsKeywordFilters"),
                violation<CorrectType>("_customData"),
                violation<CorrectType>("_BPMChanges")
            ),
            ex.constraintViolations
        )
    }

    @Test
    fun missing() {
        val ex = validateFolder("2_2/missing")
        assertNotNull(ex)

        assertContentEquals(
            listOf<Any>(
                violation<NodePresent>("version"),
                violation<NodePresent>("_notes"),
                violation<NodePresent>("_obstacles"),
                violation<NodePresent>("_events")
            ),
            ex.constraintViolations
        )
    }

    @Test
    fun `null`() {
        val ex = validateFolder("2_2/null")
        assertNotNull(ex)

        assertContentEquals(
            listOf<Any>(
                violation<NotNull>("version"),
                violation<NotNull>("_notes"),
                violation<NotNull>("_obstacles"),
                violation<NotNull>("_events"),
                violation<NotNull>("_waypoints"),
                violation<NotNull>("_specialEventsKeywordFilters"),
                violation<NotNull>("_customData"),
                violation<NotNull>("_BPMChanges")
            ),
            ex.constraintViolations
        )
    }
}
