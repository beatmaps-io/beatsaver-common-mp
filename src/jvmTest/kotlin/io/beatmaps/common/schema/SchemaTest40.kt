package io.beatmaps.common.schema

import io.beatmaps.common.api.ECharacteristic
import io.beatmaps.common.api.EDifficulty
import io.beatmaps.common.beatsaber.CorrectType
import io.beatmaps.common.beatsaber.NodePresent
import io.beatmaps.common.schema.SchemaCommon.validateFolder
import io.beatmaps.common.schema.SchemaCommon.violation4
import io.beatmaps.common.schema.SchemaCommon.violation4L
import org.junit.Test
import org.valiktor.constraints.In
import org.valiktor.constraints.NotNull
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SchemaTest40 {
    @Test
    fun badtypes() {
        val ex = validateFolder("4_0/badtypes")
        assertNotNull(ex)

        assertContentEquals(
            listOf(
                violation4<CorrectType>("version"),
                violation4<CorrectType>("colorNotes"),
                violation4<CorrectType>("colorNotesData"),
                violation4<CorrectType>("bombNotes"),
                violation4<CorrectType>("bombNotesData"),
                violation4<CorrectType>("obstacles"),
                violation4<CorrectType>("obstaclesData"),
                violation4<CorrectType>("arcs"),
                violation4<CorrectType>("arcsData"),
                violation4<CorrectType>("chains"),
                violation4<CorrectType>("chainsData"),
                violation4<CorrectType>("spawnRotations"),
                violation4<CorrectType>("spawnRotationsData"),

                violation4L<CorrectType>("version"),
                violation4L<CorrectType>("basicEvents"),
                violation4L<CorrectType>("basicEventsData"),
                violation4L<CorrectType>("colorBoostEvents"),
                violation4L<CorrectType>("colorBoostEventsData"),
                violation4L<CorrectType>("waypoints"),
                violation4L<CorrectType>("waypointsData"),
                violation4L<CorrectType>("basicEventTypesWithKeywords"),
                violation4L<CorrectType>("eventBoxGroups"),
                violation4L<CorrectType>("indexFilters"),
                violation4L<CorrectType>("lightColorEventBoxes"),
                violation4L<CorrectType>("lightColorEvents"),
                violation4L<CorrectType>("lightRotationEventBoxes"),
                violation4L<CorrectType>("lightRotationEvents"),
                violation4L<CorrectType>("lightTranslationEventBoxes"),
                violation4L<CorrectType>("lightTranslationEvents"),
                violation4L<CorrectType>("fxEventBoxes"),
                violation4L<CorrectType>("floatFxEvents"),
                violation4L<CorrectType>("useNormalEventsAsCompatibleEvents")
            ),
            ex.constraintViolations
        )
    }

    @Test
    fun basic() {
        val ex = validateFolder("4_0/basic")
        assertNull(ex)
    }

    @Test
    fun schema() {
        val ex = validateFolder("4_0/default")
        assertNull(ex)
    }

    @Test
    fun stats() {
        val ex = validateFolder(
            "4_0/stats",
            listOf(
                DiffValidator(ECharacteristic.Standard, EDifficulty.Easy) { diff, lights, sli ->
                    assertEquals(1, diff.obstacleCount())
                    assertEquals(2, diff.noteCount())
                    assertEquals(6, lights.eventCount())
                    assertEquals(1, diff.bombCount())
                    assertEquals(1, diff.arcCount())
                    assertEquals(1, diff.chainCount())
                    assertEquals(2f, diff.songLength())
                    assertEquals(345, diff.maxScore())
                    assertEquals(2f, diff.mappedNps(sli))
                }
            )
        )
        assertNull(ex)
    }

    @Test
    fun missing() {
        val ex = validateFolder("4_0/missing")
        assertNotNull(ex)

        assertContentEquals(
            listOf(
                violation4<NodePresent>("colorNotes"),
                violation4<NodePresent>("colorNotesData"),
                violation4<NodePresent>("bombNotes"),
                violation4<NodePresent>("bombNotesData"),
                violation4<NodePresent>("obstacles"),
                violation4<NodePresent>("obstaclesData"),
                violation4<NodePresent>("arcs"),
                violation4<NodePresent>("arcsData"),
                violation4<NodePresent>("chains"),
                violation4<NodePresent>("chainsData"),

                violation4L<NodePresent>("basicEvents"),
                violation4L<NodePresent>("basicEventsData"),
                violation4L<NodePresent>("colorBoostEvents"),
                violation4L<NodePresent>("colorBoostEventsData"),
                violation4L<NodePresent>("waypoints"),
                violation4L<NodePresent>("waypointsData"),
                violation4L<NodePresent>("basicEventTypesWithKeywords"),
                violation4L<NodePresent>("eventBoxGroups"),
                violation4L<NodePresent>("indexFilters"),
                violation4L<NodePresent>("lightColorEventBoxes"),
                violation4L<NodePresent>("lightColorEvents"),
                violation4L<NodePresent>("lightRotationEventBoxes"),
                violation4L<NodePresent>("lightRotationEvents"),
                violation4L<NodePresent>("lightTranslationEventBoxes"),
                violation4L<NodePresent>("lightTranslationEvents"),
                violation4L<NodePresent>("fxEventBoxes"),
                violation4L<NodePresent>("floatFxEvents")
            ),
            ex.constraintViolations
        )
    }

    @Test
    fun `null`() {
        val ex = validateFolder("4_0/null")
        assertNotNull(ex)

        assertContentEquals(
            listOf(
                violation4<NotNull>("colorNotes"),
                violation4<NotNull>("colorNotesData"),
                violation4<NotNull>("bombNotes"),
                violation4<NotNull>("bombNotesData"),
                violation4<NotNull>("obstacles"),
                violation4<NotNull>("obstaclesData"),
                violation4<NotNull>("arcs"),
                violation4<NotNull>("arcsData"),
                violation4<NotNull>("chains"),
                violation4<NotNull>("chainsData"),
                violation4<NotNull>("spawnRotations"),
                violation4<NotNull>("spawnRotationsData"),

                violation4L<NotNull>("basicEvents"),
                violation4L<NotNull>("basicEventsData"),
                violation4L<NotNull>("colorBoostEvents"),
                violation4L<NotNull>("colorBoostEventsData"),
                violation4L<NotNull>("waypoints"),
                violation4L<NotNull>("waypointsData"),
                violation4L<NotNull>("basicEventTypesWithKeywords"),
                violation4L<NotNull>("eventBoxGroups"),
                violation4L<NotNull>("indexFilters"),
                violation4L<NotNull>("lightColorEventBoxes"),
                violation4L<NotNull>("lightColorEvents"),
                violation4L<NotNull>("lightRotationEventBoxes"),
                violation4L<NotNull>("lightRotationEvents"),
                violation4L<NotNull>("lightTranslationEventBoxes"),
                violation4L<NotNull>("lightTranslationEvents"),
                violation4L<NotNull>("fxEventBoxes"),
                violation4L<NotNull>("floatFxEvents"),
                violation4L<NotNull>("useNormalEventsAsCompatibleEvents")
            ),
            ex.constraintViolations
        )
    }

    @Test
    fun error() {
        val ex = validateFolder("4_0/error")
        assertNotNull(ex)

        assertContentEquals(
            listOf(
                violation4<CorrectType>("colorNotes[0].b(beat)"),
                violation4<CorrectType>("colorNotes[0].r(rotationLane)"),
                violation4<CorrectType>("colorNotes[0].i(index)"),
                violation4<NotNull>("colorNotes[1].b(beat)"),
                violation4<NotNull>("colorNotes[1].r(rotationLane)"),
                violation4<NotNull>("colorNotes[1].i(index)"),
                violation4<CorrectType>("colorNotes[3]"),
                violation4<NotNull>("colorNotes[4]"),

                violation4<CorrectType>("colorNotesData[0].x"),
                violation4<CorrectType>("colorNotesData[0].y"),
                violation4<CorrectType>("colorNotesData[0].c(color)"),
                violation4<CorrectType>("colorNotesData[0].d(direction)"),
                violation4<CorrectType>("colorNotesData[0].a(angleOffset)"),
                violation4<NotNull>("colorNotesData[1].x"),
                violation4<NotNull>("colorNotesData[1].y"),
                violation4<In<Int>>("colorNotesData[1].c(color)"),
                violation4<NotNull>("colorNotesData[1].d(direction)"),
                violation4<NotNull>("colorNotesData[1].a(angleOffset)"),
                violation4<CorrectType>("colorNotesData[3]"),
                violation4<NotNull>("colorNotesData[4]"),

                violation4<CorrectType>("bombNotes[0].b(beat)"),
                violation4<CorrectType>("bombNotes[0].r(rotationLane)"),
                violation4<CorrectType>("bombNotes[0].i(index)"),
                violation4<NotNull>("bombNotes[1].b(beat)"),
                violation4<NotNull>("bombNotes[1].r(rotationLane)"),
                violation4<NotNull>("bombNotes[1].i(index)"),
                violation4<CorrectType>("bombNotes[3]"),
                violation4<NotNull>("bombNotes[4]"),

                violation4<CorrectType>("bombNotesData[0].x"),
                violation4<CorrectType>("bombNotesData[0].y"),
                violation4<NotNull>("bombNotesData[1].x"),
                violation4<NotNull>("bombNotesData[1].y"),
                violation4<CorrectType>("bombNotesData[3]"),
                violation4<NotNull>("bombNotesData[4]"),

                violation4<CorrectType>("obstacles[0].b(beat)"),
                violation4<CorrectType>("obstacles[0].r(rotationLane)"),
                violation4<CorrectType>("obstacles[0].i(index)"),
                violation4<NotNull>("obstacles[1].b(beat)"),
                violation4<NotNull>("obstacles[1].r(rotationLane)"),
                violation4<NotNull>("obstacles[1].i(index)"),
                violation4<CorrectType>("obstacles[3]"),
                violation4<NotNull>("obstacles[4]"),

                violation4<CorrectType>("obstaclesData[0].d(duration)"),
                violation4<CorrectType>("obstaclesData[0].x"),
                violation4<CorrectType>("obstaclesData[0].y"),
                violation4<CorrectType>("obstaclesData[0].w(width)"),
                violation4<CorrectType>("obstaclesData[0].h(height)"),
                violation4<NotNull>("obstaclesData[1].d(duration)"),
                violation4<NotNull>("obstaclesData[1].x"),
                violation4<NotNull>("obstaclesData[1].y"),
                violation4<NotNull>("obstaclesData[1].w(width)"),
                violation4<NotNull>("obstaclesData[1].h(height)"),
                violation4<CorrectType>("obstaclesData[3]"),
                violation4<NotNull>("obstaclesData[4]"),

                violation4<CorrectType>("arcs[0].hb(beat)"),
                violation4<CorrectType>("arcs[0].tb(tailBeat)"),
                violation4<CorrectType>("arcs[0].hr(headRotationLane)"),
                violation4<CorrectType>("arcs[0].tr(tailRotationLane)"),
                violation4<CorrectType>("arcs[0].ai(index)"),
                violation4<CorrectType>("arcs[0].hi(headIndex)"),
                violation4<CorrectType>("arcs[0].ti(tailIndex)"),
                violation4<NotNull>("arcs[1].hb(beat)"),
                violation4<NotNull>("arcs[1].tb(tailBeat)"),
                violation4<NotNull>("arcs[1].hr(headRotationLane)"),
                violation4<NotNull>("arcs[1].tr(tailRotationLane)"),
                violation4<NotNull>("arcs[1].ai(index)"),
                violation4<NotNull>("arcs[1].hi(headIndex)"),
                violation4<NotNull>("arcs[1].ti(tailIndex)"),
                violation4<CorrectType>("arcs[3]"),
                violation4<NotNull>("arcs[4]"),

                violation4<CorrectType>("arcsData[0].m(headControlPointLengthMultiplier)"),
                violation4<CorrectType>("arcsData[0].tm(tailControlPointLengthMultiplier)"),
                violation4<CorrectType>("arcsData[0].a(midAnchorMode)"),
                violation4<NotNull>("arcsData[1].m(headControlPointLengthMultiplier)"),
                violation4<NotNull>("arcsData[1].tm(tailControlPointLengthMultiplier)"),
                violation4<NotNull>("arcsData[1].a(midAnchorMode)"),
                violation4<CorrectType>("arcsData[3]"),
                violation4<NotNull>("arcsData[4]"),

                violation4<CorrectType>("chains[0].hb(beat)"),
                violation4<CorrectType>("chains[0].tb(tailBeat)"),
                violation4<CorrectType>("chains[0].hr(headRotationLane)"),
                violation4<CorrectType>("chains[0].tr(tailRotationLane)"),
                violation4<CorrectType>("chains[0].ci(index)"),
                violation4<CorrectType>("chains[0].i(headIndex)"),
                violation4<NotNull>("chains[1].hb(beat)"),
                violation4<NotNull>("chains[1].tb(tailBeat)"),
                violation4<NotNull>("chains[1].hr(headRotationLane)"),
                violation4<NotNull>("chains[1].tr(tailRotationLane)"),
                violation4<NotNull>("chains[1].ci(index)"),
                violation4<NotNull>("chains[1].i(headIndex)"),
                violation4<CorrectType>("chains[3]"),
                violation4<NotNull>("chains[4]"),

                violation4<CorrectType>("chainsData[0].tx(x)"),
                violation4<CorrectType>("chainsData[0].ty(y)"),
                violation4<CorrectType>("chainsData[0].c(sliceCount)"),
                violation4<CorrectType>("chainsData[0].s(squishAmount)"),
                violation4<NotNull>("chainsData[1].tx(x)"),
                violation4<NotNull>("chainsData[1].ty(y)"),
                violation4<NotNull>("chainsData[1].c(sliceCount)"),
                violation4<NotNull>("chainsData[1].s(squishAmount)"),
                violation4<CorrectType>("chainsData[3]"),
                violation4<NotNull>("chainsData[4]"),

                violation4<CorrectType>("spawnRotations[0].b(beat)"),
                violation4<CorrectType>("spawnRotations[0].i(index)"),
                violation4<NotNull>("spawnRotations[1].b(beat)"),
                violation4<NotNull>("spawnRotations[1].i(index)"),
                violation4<CorrectType>("spawnRotations[3]"),
                violation4<NotNull>("spawnRotations[4]"),

                violation4<CorrectType>("spawnRotationsData[0].t(executionTime)"),
                violation4<CorrectType>("spawnRotationsData[0].r(rotation)"),
                violation4<NotNull>("spawnRotationsData[1].t(executionTime)"),
                violation4<NotNull>("spawnRotationsData[1].r(rotation)"),
                violation4<CorrectType>("spawnRotationsData[3]"),
                violation4<NotNull>("spawnRotationsData[4]")
            ),
            ex.constraintViolations
        )
    }
}
