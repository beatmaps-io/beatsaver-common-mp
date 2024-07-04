package io.beatmaps.common.schema

import io.beatmaps.common.api.ECharacteristic
import io.beatmaps.common.api.EDifficulty
import io.beatmaps.common.beatsaber.CorrectType
import io.beatmaps.common.beatsaber.CutDirection
import io.beatmaps.common.beatsaber.NodeNotPresent
import io.beatmaps.common.beatsaber.NodePresent
import io.beatmaps.common.schema.SchemaCommon.validateFolder
import io.beatmaps.common.schema.SchemaCommon.violation4
import io.beatmaps.common.schema.SchemaCommon.violation4L
import org.junit.Test
import org.valiktor.constraints.Between
import org.valiktor.constraints.In
import org.valiktor.constraints.Matches
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

    /*@Test
    fun missing() {
        val ex = validateFolder("4_0/missing")
        assertNotNull(ex)

        assertContentEquals(
            listOf(
                violation4<NodePresent>("bpmEvents"),
                violation4<NodePresent>("rotationEvents"),
                violation4<NodePresent>("colorNotes"),
                violation4<NodePresent>("bombNotes"),
                violation4<NodePresent>("obstacles"),
                violation4<NodePresent>("sliders"),
                violation4<NodePresent>("burstSliders"),
                violation4<NodePresent>("waypoints"),
                violation4<NodePresent>("basicBeatmapEvents"),
                violation4<NodePresent>("colorBoostBeatmapEvents"),
                violation4<NodePresent>("lightColorEventBoxGroups"),
                violation4<NodePresent>("lightRotationEventBoxGroups"),
                violation4<NodePresent>("lightTranslationEventBoxGroups"),
                violation4<NodePresent>("vfxEventBoxGroups"),
                violation4<NodePresent>("_fxEventsCollection"),
                violation4<NodePresent>("basicEventTypesWithKeywords")
            ),
            ex.constraintViolations
        )
    }*/

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

    /*@Test
    fun error() {
        val ex = validateFolder("4_0/error")
        assertNotNull(ex)

        assertContentEquals(
            listOf(
                violation4<Matches>("version"),

                violation4<CorrectType>("bpmEvents[0].m(bpm)"),
                violation4<CorrectType>("bpmEvents[0].b(beat)"),
                violation4<NotNull>("bpmEvents[1].m(bpm)"),
                violation4<NotNull>("bpmEvents[1].b(beat)"),
                violation4<CorrectType>("bpmEvents[2]"),
                violation4<NotNull>("bpmEvents[3]"),

                violation4<In<Int>>("rotationEvents[0].e(executionTime)"),
                violation4<CorrectType>("rotationEvents[1].e(executionTime)"),
                violation4<CorrectType>("rotationEvents[1].b(beat)"),
                violation4<CorrectType>("rotationEvents[1].r(rotation)"),
                violation4<In<Int>>("rotationEvents[2].e(executionTime)"),
                violation4<NotNull>("rotationEvents[2].b(beat)"),
                violation4<NotNull>("rotationEvents[2].r(rotation)"),
                violation4<CorrectType>("rotationEvents[3]"),
                violation4<NotNull>("rotationEvents[4]"),

                violation4<In<Int>>("colorNotes[0].c(color)"),
                violation4<CutDirection>("colorNotes[0].d(direction)"),
                violation4<Between<Float>>("colorNotes[0].b(beat)"),
                violation4<CorrectType>("colorNotes[1].c(color)"),
                violation4<CorrectType>("colorNotes[1].d(direction)"),
                violation4<CorrectType>("colorNotes[1].b(beat)"),
                violation4<CorrectType>("colorNotes[1].x"),
                violation4<CorrectType>("colorNotes[1].y"),
                violation4<CorrectType>("colorNotes[1].a(angleOffset)"),
                violation4<In<Int>>("colorNotes[2].c(color)"),
                violation4<NotNull>("colorNotes[2].d(direction)"),
                violation4<NotNull>("colorNotes[2].b(beat)"),
                violation4<NotNull>("colorNotes[2].x"),
                violation4<NotNull>("colorNotes[2].y"),
                violation4<NotNull>("colorNotes[2].a(angleOffset)"),
                violation4<CorrectType>("colorNotes[3]"),
                violation4<NotNull>("colorNotes[4]"),

                violation4<CorrectType>("bombNotes[0].b(beat)"),
                violation4<CorrectType>("bombNotes[0].x"),
                violation4<CorrectType>("bombNotes[0].y"),
                violation4<NotNull>("bombNotes[1].b(beat)"),
                violation4<NotNull>("bombNotes[1].x"),
                violation4<NotNull>("bombNotes[1].y"),
                violation4<CorrectType>("bombNotes[2]"),
                violation4<NotNull>("bombNotes[3]"),

                violation4<CorrectType>("obstacles[0].d(duration)"),
                violation4<CorrectType>("obstacles[0].b(beat)"),
                violation4<CorrectType>("obstacles[0].x"),
                violation4<CorrectType>("obstacles[0].y"),
                violation4<CorrectType>("obstacles[0].w(width)"),
                violation4<CorrectType>("obstacles[0].h(height)"),
                violation4<NotNull>("obstacles[1].d(duration)"),
                violation4<NotNull>("obstacles[1].b(beat)"),
                violation4<NotNull>("obstacles[1].x"),
                violation4<NotNull>("obstacles[1].y"),
                violation4<NotNull>("obstacles[1].w(width)"),
                violation4<NotNull>("obstacles[1].h(height)"),
                violation4<CorrectType>("obstacles[2]"),
                violation4<NotNull>("obstacles[3]"),

                violation4<In<Int>>("sliders[0].c(color)"),
                violation4<CorrectType>("sliders[1].b(beat)"),
                violation4<CorrectType>("sliders[1].c(color)"),
                violation4<CorrectType>("sliders[1].x"),
                violation4<CorrectType>("sliders[1].y"),
                violation4<CorrectType>("sliders[1].d(direction)"),
                violation4<CorrectType>("sliders[1].tb(tailBeat)"),
                violation4<CorrectType>("sliders[1].tx(tailX)"),
                violation4<CorrectType>("sliders[1].ty(tailY)"),
                violation4<CorrectType>("sliders[1].mu(headControlPointLengthMultiplier)"),
                violation4<CorrectType>("sliders[1].tmu(tailControlPointLengthMultiplier)"),
                violation4<CorrectType>("sliders[1].tc(tailCutDirection)"),
                violation4<CorrectType>("sliders[1].m(sliderMidAnchorMode)"),
                violation4<NotNull>("sliders[2].b(beat)"),
                violation4<In<Int>>("sliders[2].c(color)"),
                violation4<NotNull>("sliders[2].x"),
                violation4<NotNull>("sliders[2].y"),
                violation4<NotNull>("sliders[2].d(direction)"),
                violation4<NotNull>("sliders[2].tb(tailBeat)"),
                violation4<NotNull>("sliders[2].tx(tailX)"),
                violation4<NotNull>("sliders[2].ty(tailY)"),
                violation4<NotNull>("sliders[2].mu(headControlPointLengthMultiplier)"),
                violation4<NotNull>("sliders[2].tmu(tailControlPointLengthMultiplier)"),
                violation4<NotNull>("sliders[2].tc(tailCutDirection)"),
                violation4<NotNull>("sliders[2].m(sliderMidAnchorMode)"),
                violation4<CorrectType>("sliders[3]"),
                violation4<NotNull>("sliders[4]"),

                violation4<Between<Float>>("burstSliders[0].b(beat)"),
                violation4<In<Int>>("burstSliders[0].c(color)"),
                violation4<Between<Float>>("burstSliders[0].tb(tailBeat)"),
                violation4<CorrectType>("burstSliders[1].b(beat)"),
                violation4<CorrectType>("burstSliders[1].c(color)"),
                violation4<CorrectType>("burstSliders[1].x"),
                violation4<CorrectType>("burstSliders[1].y"),
                violation4<CorrectType>("burstSliders[1].d(direction)"),
                violation4<CorrectType>("burstSliders[1].tb(tailBeat)"),
                violation4<CorrectType>("burstSliders[1].tx(tailX)"),
                violation4<CorrectType>("burstSliders[1].ty(tailY)"),
                violation4<CorrectType>("burstSliders[1].sc(sliceCount)"),
                violation4<CorrectType>("burstSliders[1].s(squishAmount)"),
                violation4<NotNull>("burstSliders[2].b(beat)"),
                violation4<In<Int>>("burstSliders[2].c(color)"),
                violation4<NotNull>("burstSliders[2].x"),
                violation4<NotNull>("burstSliders[2].y"),
                violation4<NotNull>("burstSliders[2].d(direction)"),
                violation4<NotNull>("burstSliders[2].tb(tailBeat)"),
                violation4<NotNull>("burstSliders[2].tx(tailX)"),
                violation4<NotNull>("burstSliders[2].ty(tailY)"),
                violation4<NotNull>("burstSliders[2].sc(sliceCount)"),
                violation4<NotNull>("burstSliders[2].s(squishAmount)"),
                violation4<CorrectType>("burstSliders[3]"),
                violation4<NotNull>("burstSliders[4]"),

                violation4<CorrectType>("waypoints[0].b(beat)"),
                violation4<CorrectType>("waypoints[0].x"),
                violation4<CorrectType>("waypoints[0].y"),
                violation4<CorrectType>("waypoints[0].d(offsetDirection)"),
                violation4<NotNull>("waypoints[1].b(beat)"),
                violation4<NotNull>("waypoints[1].x"),
                violation4<NotNull>("waypoints[1].y"),
                violation4<NotNull>("waypoints[1].d(offsetDirection)"),
                violation4<CorrectType>("waypoints[2]"),
                violation4<NotNull>("waypoints[3]"),

                violation4<CorrectType>("basicBeatmapEvents[0].b(beat)"),
                violation4<CorrectType>("basicBeatmapEvents[0].et(eventType)"),
                violation4<CorrectType>("basicBeatmapEvents[0].i(value)"),
                violation4<CorrectType>("basicBeatmapEvents[0].f(floatValue)"),
                violation4<NotNull>("basicBeatmapEvents[1].b(beat)"),
                violation4<NotNull>("basicBeatmapEvents[1].et(eventType)"),
                violation4<NotNull>("basicBeatmapEvents[1].i(value)"),
                violation4<NotNull>("basicBeatmapEvents[1].f(floatValue)"),
                violation4<CorrectType>("basicBeatmapEvents[2]"),
                violation4<NotNull>("basicBeatmapEvents[3]"),

                violation4<CorrectType>("colorBoostBeatmapEvents[0].b(beat)"),
                violation4<CorrectType>("colorBoostBeatmapEvents[0].o(boost)"),
                violation4<NotNull>("colorBoostBeatmapEvents[1].b(beat)"),
                violation4<NotNull>("colorBoostBeatmapEvents[1].o(boost)"),
                violation4<CorrectType>("colorBoostBeatmapEvents[2]"),
                violation4<NotNull>("colorBoostBeatmapEvents[3]"),

                violation4<CorrectType>("lightColorEventBoxGroups[0].b(beat)"),
                violation4<CorrectType>("lightColorEventBoxGroups[0].g(groupId)"),
                violation4<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).f(type)"),
                violation4<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).p(param0)"),
                violation4<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).t(param1)"),
                violation4<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).r(reversed)"),
                violation4<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).c(chunks)"),
                violation4<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).n(randomType)"),
                violation4<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).s(seed)"),
                violation4<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).l(limit)"),
                violation4<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).d(alsoAffectsType)"),
                violation4<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].w(beatDistributionParam)"),
                violation4<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].d(beatDistributionParamType)"),
                violation4<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].r(brightnessDistributionParam)"),
                violation4<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].t(brightnessDistributionParamType)"),
                violation4<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].b(brightnessDistributionShouldAffectFirstBaseEvent)"),
                violation4<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].e(lightColorBaseDataList)[0].b(beat)"),
                violation4<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].e(lightColorBaseDataList)[0].i(transitionType)"),
                violation4<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].e(lightColorBaseDataList)[0].c(colorType)"),
                violation4<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].e(lightColorBaseDataList)[0].s(brightness)"),
                violation4<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].e(lightColorBaseDataList)[0].f(strobeFrequency)"),
                violation4<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].e(lightColorBaseDataList)[2]"),
                violation4<NotNull>("lightColorEventBoxGroups[0].e(eventBoxes)[0].e(lightColorBaseDataList)[3]"),
                violation4<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[1].f(indexFilter)"),
                violation4<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[1].e(lightColorBaseDataList)"),
                violation4<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[2].f(indexFilter)"),
                violation4<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[3]"),
                violation4<NotNull>("lightColorEventBoxGroups[0].e(eventBoxes)[4]"),
                violation4<CorrectType>("lightColorEventBoxGroups[1].e(eventBoxes)"),
                violation4<NotNull>("lightColorEventBoxGroups[2].b(beat)"),
                violation4<NotNull>("lightColorEventBoxGroups[2].g(groupId)"),
                violation4<NotNull>("lightColorEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).f(type)"),
                violation4<NotNull>("lightColorEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).p(param0)"),
                violation4<NotNull>("lightColorEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).t(param1)"),
                violation4<NotNull>("lightColorEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).r(reversed)"),
                violation4<NotNull>("lightColorEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).c(chunks)"),
                violation4<NotNull>("lightColorEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).n(randomType)"),
                violation4<NotNull>("lightColorEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).s(seed)"),
                violation4<NotNull>("lightColorEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).l(limit)"),
                violation4<NotNull>("lightColorEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).d(alsoAffectsType)"),
                violation4<NotNull>("lightColorEventBoxGroups[2].e(eventBoxes)[0].w(beatDistributionParam)"),
                violation4<In<Int>>("lightColorEventBoxGroups[2].e(eventBoxes)[0].d(beatDistributionParamType)"),
                violation4<NotNull>("lightColorEventBoxGroups[2].e(eventBoxes)[0].r(brightnessDistributionParam)"),
                violation4<In<Int>>("lightColorEventBoxGroups[2].e(eventBoxes)[0].t(brightnessDistributionParamType)"),
                violation4<NotNull>("lightColorEventBoxGroups[2].e(eventBoxes)[0].b(brightnessDistributionShouldAffectFirstBaseEvent)"),
                violation4<NotNull>("lightColorEventBoxGroups[2].e(eventBoxes)[0].e(lightColorBaseDataList)[0].b(beat)"),
                violation4<In<Int>>("lightColorEventBoxGroups[2].e(eventBoxes)[0].e(lightColorBaseDataList)[0].i(transitionType)"),
                violation4<In<Int>>("lightColorEventBoxGroups[2].e(eventBoxes)[0].e(lightColorBaseDataList)[0].c(colorType)"),
                violation4<NotNull>("lightColorEventBoxGroups[2].e(eventBoxes)[0].e(lightColorBaseDataList)[0].s(brightness)"),
                violation4<NotNull>("lightColorEventBoxGroups[2].e(eventBoxes)[0].e(lightColorBaseDataList)[0].f(strobeFrequency)"),
                violation4<NotNull>("lightColorEventBoxGroups[2].e(eventBoxes)[1].f(indexFilter)"),
                violation4<NotNull>("lightColorEventBoxGroups[2].e(eventBoxes)[1].e(lightColorBaseDataList)"),
                violation4<NotNull>("lightColorEventBoxGroups[3].e(eventBoxes)"),
                violation4<CorrectType>("lightColorEventBoxGroups[4]"),
                violation4<NotNull>("lightColorEventBoxGroups[5]"),

                violation4<CorrectType>("lightRotationEventBoxGroups[0].b(beat)"),
                violation4<CorrectType>("lightRotationEventBoxGroups[0].g(groupId)"),
                violation4<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).f(type)"),
                violation4<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).p(param0)"),
                violation4<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).t(param1)"),
                violation4<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).r(reversed)"),
                violation4<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).c(chunks)"),
                violation4<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).n(randomType)"),
                violation4<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).s(seed)"),
                violation4<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).l(limit)"),
                violation4<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).d(alsoAffectsType)"),
                violation4<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].w(beatDistributionParam)"),
                violation4<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].d(beatDistributionParamType)"),
                violation4<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].s(rotationDistributionParam)"),
                violation4<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].t(rotationDistributionParamType)"),
                violation4<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].a(axis)"),
                violation4<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].r(flipRotation)"),
                violation4<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].b(brightnessDistributionShouldAffectFirstBaseEvent)"),
                violation4<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].l(lightRotationBaseDataList)[0].b(beat)"),
                violation4<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].l(lightRotationBaseDataList)[0].p(usePreviousEventRotationValue)"),
                violation4<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].l(lightRotationBaseDataList)[0].e(easeType)"),
                violation4<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].l(lightRotationBaseDataList)[0].l(loopsCount)"),
                violation4<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].l(lightRotationBaseDataList)[0].r(rotation)"),
                violation4<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].l(lightRotationBaseDataList)[0].o(rotationDirection)"),
                violation4<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].l(lightRotationBaseDataList)[2]"),
                violation4<NotNull>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].l(lightRotationBaseDataList)[3]"),
                violation4<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[1].f(indexFilter)"),
                violation4<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[1].l(lightRotationBaseDataList)"),
                violation4<NodePresent>("lightRotationEventBoxGroups[0].e(eventBoxes)[2].f(indexFilter)"),
                violation4<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[3]"),
                violation4<NotNull>("lightRotationEventBoxGroups[0].e(eventBoxes)[4]"),
                violation4<CorrectType>("lightRotationEventBoxGroups[1].e(eventBoxes)"),
                violation4<NotNull>("lightRotationEventBoxGroups[2].b(beat)"),
                violation4<NotNull>("lightRotationEventBoxGroups[2].g(groupId)"),
                violation4<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).f(type)"),
                violation4<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).p(param0)"),
                violation4<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).t(param1)"),
                violation4<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).r(reversed)"),
                violation4<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).c(chunks)"),
                violation4<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).n(randomType)"),
                violation4<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).s(seed)"),
                violation4<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).l(limit)"),
                violation4<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).d(alsoAffectsType)"),
                violation4<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].w(beatDistributionParam)"),
                violation4<In<Int>>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].d(beatDistributionParamType)"),
                violation4<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].s(rotationDistributionParam)"),
                violation4<In<Int>>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].t(rotationDistributionParamType)"),
                violation4<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].a(axis)"),
                violation4<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].r(flipRotation)"),
                violation4<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].b(brightnessDistributionShouldAffectFirstBaseEvent)"),
                violation4<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].l(lightRotationBaseDataList)[0].b(beat)"),
                violation4<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].l(lightRotationBaseDataList)[0].p(usePreviousEventRotationValue)"),
                violation4<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].l(lightRotationBaseDataList)[0].e(easeType)"),
                violation4<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].l(lightRotationBaseDataList)[0].l(loopsCount)"),
                violation4<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].l(lightRotationBaseDataList)[0].r(rotation)"),
                violation4<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].l(lightRotationBaseDataList)[0].o(rotationDirection)"),
                violation4<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[1].f(indexFilter)"),
                violation4<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[1].l(lightRotationBaseDataList)"),
                violation4<NotNull>("lightRotationEventBoxGroups[3].e(eventBoxes)"),
                violation4<CorrectType>("lightRotationEventBoxGroups[4]"),
                violation4<NotNull>("lightRotationEventBoxGroups[5]"),

                violation4<CorrectType>("lightTranslationEventBoxGroups[0].b(beat)"),
                violation4<CorrectType>("lightTranslationEventBoxGroups[0].g(groupId)"),
                violation4<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).f(type)"),
                violation4<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).p(param0)"),
                violation4<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).t(param1)"),
                violation4<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).r(reversed)"),
                violation4<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).c(chunks)"),
                violation4<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).n(randomType)"),
                violation4<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).s(seed)"),
                violation4<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).l(limit)"),
                violation4<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).d(alsoAffectsType)"),
                violation4<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].s(gapDistributionParam)"),
                violation4<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].t(gapDistributionParamType)"),
                violation4<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].a(axis)"),
                violation4<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].r(flipTranslation)"),
                violation4<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].b(gapDistributionShouldAffectFirstBaseEvent)"),
                violation4<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].i(gapDistributionEaseType)"),
                violation4<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].l(lightTranslationBaseDataList)[0].b(beat)"),
                violation4<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].l(lightTranslationBaseDataList)[0].p(usePreviousEventTranslationValue)"),
                violation4<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].l(lightTranslationBaseDataList)[0].e(easeType)"),
                violation4<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].l(lightTranslationBaseDataList)[0].t(translation)"),
                violation4<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].l(lightTranslationBaseDataList)[2]"),
                violation4<NotNull>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].l(lightTranslationBaseDataList)[3]"),
                violation4<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[1].f(indexFilter)"),
                violation4<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[1].l(lightTranslationBaseDataList)"),
                violation4<NodePresent>("lightTranslationEventBoxGroups[0].e(eventBoxes)[2].f(indexFilter)"),
                violation4<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[3]"),
                violation4<NotNull>("lightTranslationEventBoxGroups[0].e(eventBoxes)[4]"),
                violation4<CorrectType>("lightTranslationEventBoxGroups[1].e(eventBoxes)"),
                violation4<NotNull>("lightTranslationEventBoxGroups[2].b(beat)"),
                violation4<NotNull>("lightTranslationEventBoxGroups[2].g(groupId)"),
                violation4<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).f(type)"),
                violation4<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).p(param0)"),
                violation4<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).t(param1)"),
                violation4<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).r(reversed)"),
                violation4<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).c(chunks)"),
                violation4<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).n(randomType)"),
                violation4<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).s(seed)"),
                violation4<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).l(limit)"),
                violation4<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).d(alsoAffectsType)"),
                violation4<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[0].s(gapDistributionParam)"),
                violation4<In<Int>>("lightTranslationEventBoxGroups[2].e(eventBoxes)[0].t(gapDistributionParamType)"),
                violation4<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[0].a(axis)"),
                violation4<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[0].r(flipTranslation)"),
                violation4<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[0].b(gapDistributionShouldAffectFirstBaseEvent)"),
                violation4<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[0].i(gapDistributionEaseType)"),
                violation4<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[0].l(lightTranslationBaseDataList)[0].b(beat)"),
                violation4<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[0].l(lightTranslationBaseDataList)[0].p(usePreviousEventTranslationValue)"),
                violation4<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[0].l(lightTranslationBaseDataList)[0].e(easeType)"),
                violation4<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[0].l(lightTranslationBaseDataList)[0].t(translation)"),
                violation4<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[1].f(indexFilter)"),
                violation4<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[1].l(lightTranslationBaseDataList)"),
                violation4<NotNull>("lightTranslationEventBoxGroups[3].e(eventBoxes)"),
                violation4<CorrectType>("lightTranslationEventBoxGroups[4]"),
                violation4<NotNull>("lightTranslationEventBoxGroups[5]"),

                violation4<CorrectType>("vfxEventBoxGroups[0].b(beat)"),
                violation4<CorrectType>("vfxEventBoxGroups[0].g(groupId)"),
                violation4<CorrectType>("vfxEventBoxGroups[0].t(type)"),
                violation4<CorrectType>("vfxEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).f(type)"),
                violation4<CorrectType>("vfxEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).p(param0)"),
                violation4<CorrectType>("vfxEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).t(param1)"),
                violation4<CorrectType>("vfxEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).r(reversed)"),
                violation4<CorrectType>("vfxEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).c(chunks)"),
                violation4<CorrectType>("vfxEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).n(randomType)"),
                violation4<CorrectType>("vfxEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).s(seed)"),
                violation4<CorrectType>("vfxEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).l(limit)"),
                violation4<CorrectType>("vfxEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).d(alsoAffectsType)"),
                violation4<CorrectType>("vfxEventBoxGroups[0].e(eventBoxes)[0].w(beatDistributionParam)"),
                violation4<CorrectType>("vfxEventBoxGroups[0].e(eventBoxes)[0].d(beatDistributionParamType)"),
                violation4<CorrectType>("vfxEventBoxGroups[0].e(eventBoxes)[0].s(vfxDistributionParam)"),
                violation4<CorrectType>("vfxEventBoxGroups[0].e(eventBoxes)[0].t(vfxDistributionParamType)"),
                violation4<CorrectType>("vfxEventBoxGroups[0].e(eventBoxes)[0].i(vfxDistributionEaseType)"),
                violation4<CorrectType>("vfxEventBoxGroups[0].e(eventBoxes)[0].b(vfxDistributionShouldAffectFirstBaseEvent)"),
                violation4<CorrectType>("vfxEventBoxGroups[0].e(eventBoxes)[0].l(vfxBaseDataList)[0]"),
                violation4<NotNull>("vfxEventBoxGroups[0].e(eventBoxes)[0].l(vfxBaseDataList)[1]"),
                violation4<CorrectType>("vfxEventBoxGroups[0].e(eventBoxes)[1].f(indexFilter)"),
                violation4<CorrectType>("vfxEventBoxGroups[0].e(eventBoxes)[1].l(vfxBaseDataList)"),
                violation4<NodePresent>("vfxEventBoxGroups[0].e(eventBoxes)[2].f(indexFilter)"),
                violation4<CorrectType>("vfxEventBoxGroups[0].e(eventBoxes)[3]"),
                violation4<NotNull>("vfxEventBoxGroups[0].e(eventBoxes)[4]"),
                violation4<CorrectType>("vfxEventBoxGroups[1].e(eventBoxes)"),
                violation4<NotNull>("vfxEventBoxGroups[2].b(beat)"),
                violation4<NotNull>("vfxEventBoxGroups[2].g(groupId)"),
                violation4<In<Int>>("vfxEventBoxGroups[2].t(type)"),
                violation4<NotNull>("vfxEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).f(type)"),
                violation4<NotNull>("vfxEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).p(param0)"),
                violation4<NotNull>("vfxEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).t(param1)"),
                violation4<NotNull>("vfxEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).r(reversed)"),
                violation4<NotNull>("vfxEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).c(chunks)"),
                violation4<NotNull>("vfxEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).n(randomType)"),
                violation4<NotNull>("vfxEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).s(seed)"),
                violation4<NotNull>("vfxEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).l(limit)"),
                violation4<NotNull>("vfxEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).d(alsoAffectsType)"),
                violation4<NotNull>("vfxEventBoxGroups[2].e(eventBoxes)[0].w(beatDistributionParam)"),
                violation4<In<Int>>("vfxEventBoxGroups[2].e(eventBoxes)[0].d(beatDistributionParamType)"),
                violation4<NotNull>("vfxEventBoxGroups[2].e(eventBoxes)[0].s(vfxDistributionParam)"),
                violation4<In<Int>>("vfxEventBoxGroups[2].e(eventBoxes)[0].t(vfxDistributionParamType)"),
                violation4<NotNull>("vfxEventBoxGroups[2].e(eventBoxes)[0].i(vfxDistributionEaseType)"),
                violation4<NotNull>("vfxEventBoxGroups[2].e(eventBoxes)[0].b(vfxDistributionShouldAffectFirstBaseEvent)"),
                violation4<NotNull>("vfxEventBoxGroups[2].e(eventBoxes)[0].l(vfxBaseDataList)"),
                violation4<NotNull>("vfxEventBoxGroups[2].e(eventBoxes)[1].f(indexFilter)"),
                violation4<NotNull>("vfxEventBoxGroups[3].e(eventBoxes)"),
                violation4<CorrectType>("vfxEventBoxGroups[5]"),
                violation4<NotNull>("vfxEventBoxGroups[6]"),

                violation4<CorrectType>("_fxEventsCollection._il(intEventsList)[0].b(beat)"),
                violation4<CorrectType>("_fxEventsCollection._il(intEventsList)[0].p(usePreviousEventValue)"),
                violation4<CorrectType>("_fxEventsCollection._il(intEventsList)[0].v(value)"),
                violation4<NotNull>("_fxEventsCollection._il(intEventsList)[1].b(beat)"),
                violation4<NotNull>("_fxEventsCollection._il(intEventsList)[1].p(usePreviousEventValue)"),
                violation4<NotNull>("_fxEventsCollection._il(intEventsList)[1].v(value)"),
                violation4<CorrectType>("_fxEventsCollection._il(intEventsList)[3]"),
                violation4<NotNull>("_fxEventsCollection._il(intEventsList)[4]"),

                violation4<CorrectType>("_fxEventsCollection._fl(floatEventsList)[0].b(beat)"),
                violation4<CorrectType>("_fxEventsCollection._fl(floatEventsList)[0].p(usePreviousEventValue)"),
                violation4<CorrectType>("_fxEventsCollection._fl(floatEventsList)[0].v(value)"),
                violation4<CorrectType>("_fxEventsCollection._fl(floatEventsList)[0].i(easeType)"),
                violation4<NotNull>("_fxEventsCollection._fl(floatEventsList)[1].b(beat)"),
                violation4<NotNull>("_fxEventsCollection._fl(floatEventsList)[1].p(usePreviousEventValue)"),
                violation4<NotNull>("_fxEventsCollection._fl(floatEventsList)[1].v(value)"),
                violation4<NotNull>("_fxEventsCollection._fl(floatEventsList)[1].i(easeType)"),
                violation4<CorrectType>("_fxEventsCollection._fl(floatEventsList)[3]"),
                violation4<NotNull>("_fxEventsCollection._fl(floatEventsList)[4]"),

                violation4<CorrectType>("useNormalEventsAsCompatibleEvents")
            ),
            ex.constraintViolations
        )
    }*/
}
