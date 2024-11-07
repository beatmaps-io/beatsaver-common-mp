package io.beatmaps.common.schema

import io.beatmaps.common.api.ECharacteristic
import io.beatmaps.common.api.EDifficulty
import io.beatmaps.common.beatsaber.CorrectType
import io.beatmaps.common.beatsaber.CutDirection
import io.beatmaps.common.beatsaber.NodeNotPresent
import io.beatmaps.common.beatsaber.NodePresent
import io.beatmaps.common.schema.SchemaCommon.validateFolder
import io.beatmaps.common.schema.SchemaCommon.violation
import org.junit.Test
import org.valiktor.constraints.Between
import org.valiktor.constraints.In
import org.valiktor.constraints.Matches
import org.valiktor.constraints.NotNull
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SchemaTest33 {
    @Test
    fun schemaAs3_3() {
        val ex = validateFolder("3_2/as3_3")
        assertNotNull(ex)

        assertContentEquals(
            listOf(
                violation<NodePresent>("vfxEventBoxGroups"),
                violation<NodePresent>("_fxEventsCollection")
            ),
            ex.constraintViolations
        )
    }

    @Test
    fun bullet() {
        val ex = validateFolder("3_3/bullet")
        assertNull(ex)
    }

    @Test
    fun badtypes() {
        val ex = validateFolder("3_3/badtypes")
        assertNotNull(ex)

        assertContentEquals(
            listOf(
                violation<CorrectType>("version"),
                violation<CorrectType>("bpmEvents"),
                violation<CorrectType>("rotationEvents"),
                violation<CorrectType>("colorNotes"),
                violation<CorrectType>("bombNotes"),
                violation<CorrectType>("obstacles"),
                violation<CorrectType>("sliders"),
                violation<CorrectType>("burstSliders"),
                violation<CorrectType>("waypoints"),
                violation<CorrectType>("basicBeatmapEvents"),
                violation<CorrectType>("colorBoostBeatmapEvents"),
                violation<CorrectType>("lightColorEventBoxGroups"),
                violation<CorrectType>("lightRotationEventBoxGroups"),
                violation<CorrectType>("lightTranslationEventBoxGroups"),
                violation<CorrectType>("vfxEventBoxGroups"),
                violation<CorrectType>("_fxEventsCollection"),
                violation<CorrectType>("basicEventTypesWithKeywords"),
                violation<CorrectType>("useNormalEventsAsCompatibleEvents")
            ),
            ex.constraintViolations
        )
    }

    @Test
    fun basic() {
        val ex = validateFolder("3_3/basic")
        assertNull(ex)
    }

    @Test
    fun schema() {
        val ex = validateFolder("3_3/default")
        assertNull(ex)
    }

    @Test
    fun stats() {
        val ex = validateFolder(
            "3_3/stats",
            listOf(
                DiffValidator(ECharacteristic.Standard, EDifficulty.Easy) { diff, lights, sli ->
                    assertEquals(1, diff.obstacleCount())
                    assertEquals(2, diff.noteCount())
                    assertEquals(3, lights.eventCount())
                    assertEquals(1, diff.bombCount())
                    assertEquals(1, diff.arcCount())
                    assertEquals(1, diff.chainCount())
                    assertEquals(16f, diff.songLength())
                    assertEquals(345, diff.maxScore())
                    assertEquals(0.25f, diff.mappedNps(sli))
                }
            )
        )
        assertNull(ex)
    }

    @Test
    fun missing() {
        val ex = validateFolder("3_3/missing")
        assertNotNull(ex)

        assertContentEquals(
            listOf(
                violation<NodePresent>("bpmEvents"),
                violation<NodePresent>("rotationEvents"),
                violation<NodePresent>("colorNotes"),
                violation<NodePresent>("bombNotes"),
                violation<NodePresent>("obstacles"),
                violation<NodePresent>("sliders"),
                violation<NodePresent>("burstSliders"),
                violation<NodePresent>("waypoints"),
                violation<NodePresent>("basicBeatmapEvents"),
                violation<NodePresent>("colorBoostBeatmapEvents"),
                violation<NodePresent>("lightColorEventBoxGroups"),
                violation<NodePresent>("lightRotationEventBoxGroups"),
                violation<NodePresent>("lightTranslationEventBoxGroups"),
                violation<NodePresent>("vfxEventBoxGroups"),
                violation<NodePresent>("_fxEventsCollection"),
                violation<NodePresent>("basicEventTypesWithKeywords")
            ),
            ex.constraintViolations
        )
    }

    @Test
    fun schemaAs3_2() {
        val ex = validateFolder("3_3/as3_2")
        assertNotNull(ex)

        assertContentEquals(
            listOf(
                violation<NodePresent>("bpmEvents[0].b(beat)"),

                violation<NodePresent>("colorNotes[0].b(beat)"),
                violation<NodePresent>("colorNotes[0].y"),
                violation<NodePresent>("colorNotes[0].a(angleOffset)"),
                violation<NodePresent>("colorNotes[1].c(color)"),
                violation<NodePresent>("colorNotes[1].y"),
                violation<NodePresent>("colorNotes[1].a(angleOffset)"),
                violation<NodePresent>("colorNotes[2].y"),
                violation<NodePresent>("colorNotes[2].a(angleOffset)"),
                violation<NodePresent>("colorNotes[4].c(color)"),
                violation<NodePresent>("colorNotes[4].x"),
                violation<NodePresent>("colorNotes[4].y"),
                violation<NodePresent>("colorNotes[4].a(angleOffset)"),
                violation<NodePresent>("colorNotes[5].c(color)"),
                violation<NodePresent>("colorNotes[5].d(direction)"),
                violation<NodePresent>("colorNotes[5].x"),
                violation<NodePresent>("colorNotes[5].y"),
                violation<NodePresent>("colorNotes[5].a(angleOffset)"),
                violation<NodePresent>("colorNotes[6].a(angleOffset)"),

                violation<NodePresent>("bombNotes[0].b(beat)"),
                violation<NodePresent>("bombNotes[0].x"),
                violation<NodePresent>("bombNotes[0].y"),
                violation<NodePresent>("bombNotes[1].x"),
                violation<NodePresent>("bombNotes[1].y"),

                violation<NodePresent>("obstacles[0].x"),
                violation<NodePresent>("obstacles[0].y"),
                violation<NodePresent>("obstacles[1].y"),

                violation<NodePresent>("sliders[0].c(color)"),
                violation<NodePresent>("sliders[0].x"),
                violation<NodePresent>("sliders[0].y"),
                violation<NodePresent>("sliders[0].tx(tailX)"),
                violation<NodePresent>("sliders[0].ty(tailY)"),
                violation<NodePresent>("sliders[0].tc(tailCutDirection)"),
                violation<NodePresent>("sliders[0].m(sliderMidAnchorMode)"),

                violation<NodePresent>("burstSliders[0].tx(tailX)"),
                violation<NodePresent>("burstSliders[0].ty(tailY)"),

                violation<NodePresent>("basicBeatmapEvents[0].b(beat)"),
                violation<NodePresent>("basicBeatmapEvents[1].b(beat)"),
                violation<NodePresent>("basicBeatmapEvents[1].i(value)"),
                violation<NodePresent>("basicBeatmapEvents[3].i(value)"),
                violation<NodePresent>("basicBeatmapEvents[6].i(value)"),
                violation<NodePresent>("basicBeatmapEvents[9].i(value)"),
                violation<NodePresent>("basicBeatmapEvents[9].f(floatValue)"),
                violation<NodePresent>("basicBeatmapEvents[10].i(value)"),
                violation<NodePresent>("basicBeatmapEvents[10].f(floatValue)"),
                violation<NodePresent>("basicBeatmapEvents[11].f(floatValue)"),
                violation<NodePresent>("basicBeatmapEvents[12].f(floatValue)"),
                violation<NodePresent>("basicBeatmapEvents[13].f(floatValue)"),

                violation<NodePresent>("colorBoostBeatmapEvents[0].b(beat)"),
                violation<NodePresent>("colorBoostBeatmapEvents[0].o(boost)"),
                violation<NodePresent>("colorBoostBeatmapEvents[2].o(boost)"),
                violation<NodePresent>("colorBoostBeatmapEvents[4].o(boost)"),

                violation<NodePresent>("lightColorEventBoxGroups[0].b(beat)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).p(param0)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).t(param1)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).r(reversed)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).c(chunks)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).n(randomType)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).s(seed)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).l(limit)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).d(alsoAffectsType)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[0].w(beatDistributionParam)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[0].r(brightnessDistributionParam)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[0].b(brightnessDistributionShouldAffectFirstBaseEvent)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[0].e(lightColorBaseDataList)[0].b(beat)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[0].e(lightColorBaseDataList)[0].i(transitionType)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[0].e(lightColorBaseDataList)[0].f(strobeFrequency)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[1].f(indexFilter).t(param1)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[1].f(indexFilter).r(reversed)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[1].f(indexFilter).c(chunks)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[1].f(indexFilter).n(randomType)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[1].f(indexFilter).s(seed)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[1].f(indexFilter).l(limit)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[1].f(indexFilter).d(alsoAffectsType)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[1].w(beatDistributionParam)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[1].r(brightnessDistributionParam)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[1].b(brightnessDistributionShouldAffectFirstBaseEvent)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[1].e(lightColorBaseDataList)[0].i(transitionType)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[1].e(lightColorBaseDataList)[0].f(strobeFrequency)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[1].e(lightColorBaseDataList)[1].i(transitionType)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[1].e(lightColorBaseDataList)[1].s(brightness)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[1].e(lightColorBaseDataList)[1].f(strobeFrequency)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[2].f(indexFilter).t(param1)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[2].f(indexFilter).r(reversed)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[2].f(indexFilter).c(chunks)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[2].f(indexFilter).n(randomType)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[2].f(indexFilter).s(seed)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[2].f(indexFilter).l(limit)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[2].f(indexFilter).d(alsoAffectsType)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[2].w(beatDistributionParam)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[2].r(brightnessDistributionParam)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[2].b(brightnessDistributionShouldAffectFirstBaseEvent)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[2].e(lightColorBaseDataList)[0].i(transitionType)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[2].e(lightColorBaseDataList)[0].f(strobeFrequency)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[2].e(lightColorBaseDataList)[1].f(strobeFrequency)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[3].f(indexFilter).t(param1)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[3].f(indexFilter).r(reversed)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[3].f(indexFilter).c(chunks)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[3].f(indexFilter).n(randomType)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[3].f(indexFilter).s(seed)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[3].f(indexFilter).l(limit)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[3].f(indexFilter).d(alsoAffectsType)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[3].w(beatDistributionParam)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[3].r(brightnessDistributionParam)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[3].b(brightnessDistributionShouldAffectFirstBaseEvent)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[3].e(lightColorBaseDataList)[0].i(transitionType)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[3].e(lightColorBaseDataList)[0].f(strobeFrequency)"),

                violation<NodePresent>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).t(param1)"),
                violation<NodePresent>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).r(reversed)"),
                violation<NodePresent>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).c(chunks)"),
                violation<NodePresent>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).n(randomType)"),
                violation<NodePresent>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).s(seed)"),
                violation<NodePresent>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).l(limit)"),
                violation<NodePresent>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).d(alsoAffectsType)"),
                violation<NodePresent>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].w(beatDistributionParam)"),
                violation<NodePresent>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].s(rotationDistributionParam)"),
                violation<NodePresent>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].r(flipRotation)"),
                violation<NodePresent>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].b(brightnessDistributionShouldAffectFirstBaseEvent)"),
                violation<NodePresent>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].l(lightRotationBaseDataList)[0].b(beat)"),
                violation<NodePresent>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].l(lightRotationBaseDataList)[0].p(usePreviousEventRotationValue)"),
                violation<NodePresent>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].l(lightRotationBaseDataList)[0].l(loopsCount)"),
                violation<NodePresent>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].l(lightRotationBaseDataList)[0].o(rotationDirection)"),

                violation<NodePresent>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).t(param1)"),
                violation<NodePresent>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).r(reversed)"),
                violation<NodePresent>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).c(chunks)"),
                violation<NodePresent>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).n(randomType)"),
                violation<NodePresent>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).s(seed)"),
                violation<NodePresent>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).l(limit)"),
                violation<NodePresent>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).d(alsoAffectsType)"),
                violation<NodePresent>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].s(gapDistributionParam)"),
                violation<NodePresent>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].r(flipTranslation)"),
                violation<NodePresent>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].b(gapDistributionShouldAffectFirstBaseEvent)"),
                violation<NodePresent>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].i(gapDistributionEaseType)"),
                violation<NodePresent>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].l(lightTranslationBaseDataList)[0].b(beat)"),
                violation<NodePresent>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].l(lightTranslationBaseDataList)[0].p(usePreviousEventTranslationValue)"),
                violation<NodePresent>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].l(lightTranslationBaseDataList)[0].t(translation)"),

                violation<NodeNotPresent>("vfxEventBoxGroups"),
                violation<NodeNotPresent>("_fxEventsCollection"),

                violation<NodePresent>("useNormalEventsAsCompatibleEvents")
            ),
            ex.constraintViolations
        )
    }

    @Test
    fun `null`() {
        val ex = validateFolder("3_3/null")
        assertNotNull(ex)

        assertContentEquals(
            listOf(
                violation<NotNull>("version"),
                violation<NotNull>("bpmEvents"),
                violation<NotNull>("rotationEvents"),
                violation<NotNull>("colorNotes"),
                violation<NotNull>("bombNotes"),
                violation<NotNull>("obstacles"),
                violation<NotNull>("sliders"),
                violation<NotNull>("burstSliders"),
                violation<NotNull>("waypoints"),
                violation<NotNull>("basicBeatmapEvents"),
                violation<NotNull>("colorBoostBeatmapEvents"),
                violation<NotNull>("lightColorEventBoxGroups"),
                violation<NotNull>("lightRotationEventBoxGroups"),
                violation<NotNull>("lightTranslationEventBoxGroups"),
                violation<NotNull>("vfxEventBoxGroups"),
                violation<NotNull>("_fxEventsCollection"),
                violation<NotNull>("basicEventTypesWithKeywords"),
                violation<NotNull>("useNormalEventsAsCompatibleEvents")
            ),
            ex.constraintViolations
        )
    }

    @Test
    fun error() {
        val ex = validateFolder("3_3/error")
        assertNotNull(ex)

        assertContentEquals(
            listOf(
                violation<Matches>("version"),

                violation<CorrectType>("bpmEvents[0].m(bpm)"),
                violation<CorrectType>("bpmEvents[0].b(beat)"),
                violation<NotNull>("bpmEvents[1].m(bpm)"),
                violation<NotNull>("bpmEvents[1].b(beat)"),
                violation<CorrectType>("bpmEvents[2]"),
                violation<NotNull>("bpmEvents[3]"),

                violation<In<Int>>("rotationEvents[0].e(executionTime)"),
                violation<CorrectType>("rotationEvents[1].e(executionTime)"),
                violation<CorrectType>("rotationEvents[1].b(beat)"),
                violation<CorrectType>("rotationEvents[1].r(rotation)"),
                violation<In<Int>>("rotationEvents[2].e(executionTime)"),
                violation<NotNull>("rotationEvents[2].b(beat)"),
                violation<NotNull>("rotationEvents[2].r(rotation)"),
                violation<CorrectType>("rotationEvents[3]"),
                violation<NotNull>("rotationEvents[4]"),

                violation<In<Int>>("colorNotes[0].c(color)"),
                violation<CutDirection>("colorNotes[0].d(direction)"),
                violation<Between<Float>>("colorNotes[0].b(beat)"),
                violation<CorrectType>("colorNotes[1].c(color)"),
                violation<CorrectType>("colorNotes[1].d(direction)"),
                violation<CorrectType>("colorNotes[1].b(beat)"),
                violation<CorrectType>("colorNotes[1].x"),
                violation<CorrectType>("colorNotes[1].y"),
                violation<CorrectType>("colorNotes[1].a(angleOffset)"),
                violation<In<Int>>("colorNotes[2].c(color)"),
                violation<NotNull>("colorNotes[2].d(direction)"),
                violation<NotNull>("colorNotes[2].b(beat)"),
                violation<NotNull>("colorNotes[2].x"),
                violation<NotNull>("colorNotes[2].y"),
                violation<NotNull>("colorNotes[2].a(angleOffset)"),
                violation<CorrectType>("colorNotes[3]"),
                violation<NotNull>("colorNotes[4]"),

                violation<CorrectType>("bombNotes[0].b(beat)"),
                violation<CorrectType>("bombNotes[0].x"),
                violation<CorrectType>("bombNotes[0].y"),
                violation<NotNull>("bombNotes[1].b(beat)"),
                violation<NotNull>("bombNotes[1].x"),
                violation<NotNull>("bombNotes[1].y"),
                violation<CorrectType>("bombNotes[2]"),
                violation<NotNull>("bombNotes[3]"),

                violation<CorrectType>("obstacles[0].d(duration)"),
                violation<CorrectType>("obstacles[0].b(beat)"),
                violation<CorrectType>("obstacles[0].x"),
                violation<CorrectType>("obstacles[0].y"),
                violation<CorrectType>("obstacles[0].w(width)"),
                violation<CorrectType>("obstacles[0].h(height)"),
                violation<NotNull>("obstacles[1].d(duration)"),
                violation<NotNull>("obstacles[1].b(beat)"),
                violation<NotNull>("obstacles[1].x"),
                violation<NotNull>("obstacles[1].y"),
                violation<NotNull>("obstacles[1].w(width)"),
                violation<NotNull>("obstacles[1].h(height)"),
                violation<CorrectType>("obstacles[2]"),
                violation<NotNull>("obstacles[3]"),

                violation<In<Int>>("sliders[0].c(color)"),
                violation<CorrectType>("sliders[1].b(beat)"),
                violation<CorrectType>("sliders[1].c(color)"),
                violation<CorrectType>("sliders[1].x"),
                violation<CorrectType>("sliders[1].y"),
                violation<CorrectType>("sliders[1].d(direction)"),
                violation<CorrectType>("sliders[1].tb(tailBeat)"),
                violation<CorrectType>("sliders[1].tx(tailX)"),
                violation<CorrectType>("sliders[1].ty(tailY)"),
                violation<CorrectType>("sliders[1].mu(headControlPointLengthMultiplier)"),
                violation<CorrectType>("sliders[1].tmu(tailControlPointLengthMultiplier)"),
                violation<CorrectType>("sliders[1].tc(tailCutDirection)"),
                violation<CorrectType>("sliders[1].m(sliderMidAnchorMode)"),
                violation<NotNull>("sliders[2].b(beat)"),
                violation<In<Int>>("sliders[2].c(color)"),
                violation<NotNull>("sliders[2].x"),
                violation<NotNull>("sliders[2].y"),
                violation<NotNull>("sliders[2].d(direction)"),
                violation<NotNull>("sliders[2].tb(tailBeat)"),
                violation<NotNull>("sliders[2].tx(tailX)"),
                violation<NotNull>("sliders[2].ty(tailY)"),
                violation<NotNull>("sliders[2].mu(headControlPointLengthMultiplier)"),
                violation<NotNull>("sliders[2].tmu(tailControlPointLengthMultiplier)"),
                violation<NotNull>("sliders[2].tc(tailCutDirection)"),
                violation<NotNull>("sliders[2].m(sliderMidAnchorMode)"),
                violation<CorrectType>("sliders[3]"),
                violation<NotNull>("sliders[4]"),

                violation<Between<Float>>("burstSliders[0].b(beat)"),
                violation<In<Int>>("burstSliders[0].c(color)"),
                violation<Between<Float>>("burstSliders[0].tb(tailBeat)"),
                violation<CorrectType>("burstSliders[1].b(beat)"),
                violation<CorrectType>("burstSliders[1].c(color)"),
                violation<CorrectType>("burstSliders[1].x"),
                violation<CorrectType>("burstSliders[1].y"),
                violation<CorrectType>("burstSliders[1].d(direction)"),
                violation<CorrectType>("burstSliders[1].tb(tailBeat)"),
                violation<CorrectType>("burstSliders[1].tx(tailX)"),
                violation<CorrectType>("burstSliders[1].ty(tailY)"),
                violation<CorrectType>("burstSliders[1].sc(sliceCount)"),
                violation<CorrectType>("burstSliders[1].s(squishAmount)"),
                violation<NotNull>("burstSliders[2].b(beat)"),
                violation<In<Int>>("burstSliders[2].c(color)"),
                violation<NotNull>("burstSliders[2].x"),
                violation<NotNull>("burstSliders[2].y"),
                violation<NotNull>("burstSliders[2].d(direction)"),
                violation<NotNull>("burstSliders[2].tb(tailBeat)"),
                violation<NotNull>("burstSliders[2].tx(tailX)"),
                violation<NotNull>("burstSliders[2].ty(tailY)"),
                violation<NotNull>("burstSliders[2].sc(sliceCount)"),
                violation<NotNull>("burstSliders[2].s(squishAmount)"),
                violation<CorrectType>("burstSliders[3]"),
                violation<NotNull>("burstSliders[4]"),

                violation<CorrectType>("waypoints[0].b(beat)"),
                violation<CorrectType>("waypoints[0].x"),
                violation<CorrectType>("waypoints[0].y"),
                violation<CorrectType>("waypoints[0].d(offsetDirection)"),
                violation<NotNull>("waypoints[1].b(beat)"),
                violation<NotNull>("waypoints[1].x"),
                violation<NotNull>("waypoints[1].y"),
                violation<NotNull>("waypoints[1].d(offsetDirection)"),
                violation<CorrectType>("waypoints[2]"),
                violation<NotNull>("waypoints[3]"),

                violation<CorrectType>("basicBeatmapEvents[0].b(beat)"),
                violation<CorrectType>("basicBeatmapEvents[0].et(eventType)"),
                violation<CorrectType>("basicBeatmapEvents[0].i(value)"),
                violation<CorrectType>("basicBeatmapEvents[0].f(floatValue)"),
                violation<NotNull>("basicBeatmapEvents[1].b(beat)"),
                violation<NotNull>("basicBeatmapEvents[1].et(eventType)"),
                violation<NotNull>("basicBeatmapEvents[1].i(value)"),
                violation<NotNull>("basicBeatmapEvents[1].f(floatValue)"),
                violation<CorrectType>("basicBeatmapEvents[2]"),
                violation<NotNull>("basicBeatmapEvents[3]"),

                violation<CorrectType>("colorBoostBeatmapEvents[0].b(beat)"),
                violation<CorrectType>("colorBoostBeatmapEvents[0].o(boost)"),
                violation<NotNull>("colorBoostBeatmapEvents[1].b(beat)"),
                violation<NotNull>("colorBoostBeatmapEvents[1].o(boost)"),
                violation<CorrectType>("colorBoostBeatmapEvents[2]"),
                violation<NotNull>("colorBoostBeatmapEvents[3]"),

                violation<CorrectType>("lightColorEventBoxGroups[0].b(beat)"),
                violation<CorrectType>("lightColorEventBoxGroups[0].g(groupId)"),
                violation<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).f(type)"),
                violation<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).p(param0)"),
                violation<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).t(param1)"),
                violation<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).r(reversed)"),
                violation<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).c(chunks)"),
                violation<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).n(randomType)"),
                violation<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).s(seed)"),
                violation<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).l(limit)"),
                violation<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).d(alsoAffectsType)"),
                violation<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].w(beatDistributionParam)"),
                violation<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].d(beatDistributionParamType)"),
                violation<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].r(brightnessDistributionParam)"),
                violation<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].t(brightnessDistributionParamType)"),
                violation<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].b(brightnessDistributionShouldAffectFirstBaseEvent)"),
                violation<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].e(lightColorBaseDataList)[0].b(beat)"),
                violation<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].e(lightColorBaseDataList)[0].i(transitionType)"),
                violation<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].e(lightColorBaseDataList)[0].c(colorType)"),
                violation<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].e(lightColorBaseDataList)[0].s(brightness)"),
                violation<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].e(lightColorBaseDataList)[0].f(strobeFrequency)"),
                violation<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[0].e(lightColorBaseDataList)[2]"),
                violation<NotNull>("lightColorEventBoxGroups[0].e(eventBoxes)[0].e(lightColorBaseDataList)[3]"),
                violation<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[1].f(indexFilter)"),
                violation<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[1].e(lightColorBaseDataList)"),
                violation<NodePresent>("lightColorEventBoxGroups[0].e(eventBoxes)[2].f(indexFilter)"),
                violation<CorrectType>("lightColorEventBoxGroups[0].e(eventBoxes)[3]"),
                violation<NotNull>("lightColorEventBoxGroups[0].e(eventBoxes)[4]"),
                violation<CorrectType>("lightColorEventBoxGroups[1].e(eventBoxes)"),
                violation<NotNull>("lightColorEventBoxGroups[2].b(beat)"),
                violation<NotNull>("lightColorEventBoxGroups[2].g(groupId)"),
                violation<NotNull>("lightColorEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).f(type)"),
                violation<NotNull>("lightColorEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).p(param0)"),
                violation<NotNull>("lightColorEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).t(param1)"),
                violation<NotNull>("lightColorEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).r(reversed)"),
                violation<NotNull>("lightColorEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).c(chunks)"),
                violation<NotNull>("lightColorEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).n(randomType)"),
                violation<NotNull>("lightColorEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).s(seed)"),
                violation<NotNull>("lightColorEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).l(limit)"),
                violation<NotNull>("lightColorEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).d(alsoAffectsType)"),
                violation<NotNull>("lightColorEventBoxGroups[2].e(eventBoxes)[0].w(beatDistributionParam)"),
                violation<In<Int>>("lightColorEventBoxGroups[2].e(eventBoxes)[0].d(beatDistributionParamType)"),
                violation<NotNull>("lightColorEventBoxGroups[2].e(eventBoxes)[0].r(brightnessDistributionParam)"),
                violation<In<Int>>("lightColorEventBoxGroups[2].e(eventBoxes)[0].t(brightnessDistributionParamType)"),
                violation<NotNull>("lightColorEventBoxGroups[2].e(eventBoxes)[0].b(brightnessDistributionShouldAffectFirstBaseEvent)"),
                violation<NotNull>("lightColorEventBoxGroups[2].e(eventBoxes)[0].e(lightColorBaseDataList)[0].b(beat)"),
                violation<In<Int>>("lightColorEventBoxGroups[2].e(eventBoxes)[0].e(lightColorBaseDataList)[0].i(transitionType)"),
                violation<In<Int>>("lightColorEventBoxGroups[2].e(eventBoxes)[0].e(lightColorBaseDataList)[0].c(colorType)"),
                violation<NotNull>("lightColorEventBoxGroups[2].e(eventBoxes)[0].e(lightColorBaseDataList)[0].s(brightness)"),
                violation<NotNull>("lightColorEventBoxGroups[2].e(eventBoxes)[0].e(lightColorBaseDataList)[0].f(strobeFrequency)"),
                violation<NotNull>("lightColorEventBoxGroups[2].e(eventBoxes)[1].f(indexFilter)"),
                violation<NotNull>("lightColorEventBoxGroups[2].e(eventBoxes)[1].e(lightColorBaseDataList)"),
                violation<NotNull>("lightColorEventBoxGroups[3].e(eventBoxes)"),
                violation<CorrectType>("lightColorEventBoxGroups[4]"),
                violation<NotNull>("lightColorEventBoxGroups[5]"),

                violation<CorrectType>("lightRotationEventBoxGroups[0].b(beat)"),
                violation<CorrectType>("lightRotationEventBoxGroups[0].g(groupId)"),
                violation<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).f(type)"),
                violation<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).p(param0)"),
                violation<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).t(param1)"),
                violation<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).r(reversed)"),
                violation<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).c(chunks)"),
                violation<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).n(randomType)"),
                violation<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).s(seed)"),
                violation<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).l(limit)"),
                violation<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).d(alsoAffectsType)"),
                violation<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].w(beatDistributionParam)"),
                violation<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].d(beatDistributionParamType)"),
                violation<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].s(rotationDistributionParam)"),
                violation<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].t(rotationDistributionParamType)"),
                violation<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].a(axis)"),
                violation<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].r(flipRotation)"),
                violation<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].b(brightnessDistributionShouldAffectFirstBaseEvent)"),
                violation<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].l(lightRotationBaseDataList)[0].b(beat)"),
                violation<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].l(lightRotationBaseDataList)[0].p(usePreviousEventRotationValue)"),
                violation<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].l(lightRotationBaseDataList)[0].e(easeType)"),
                violation<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].l(lightRotationBaseDataList)[0].l(loopsCount)"),
                violation<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].l(lightRotationBaseDataList)[0].r(rotation)"),
                violation<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].l(lightRotationBaseDataList)[0].o(rotationDirection)"),
                violation<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].l(lightRotationBaseDataList)[2]"),
                violation<NotNull>("lightRotationEventBoxGroups[0].e(eventBoxes)[0].l(lightRotationBaseDataList)[3]"),
                violation<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[1].f(indexFilter)"),
                violation<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[1].l(lightRotationBaseDataList)"),
                violation<NodePresent>("lightRotationEventBoxGroups[0].e(eventBoxes)[2].f(indexFilter)"),
                violation<CorrectType>("lightRotationEventBoxGroups[0].e(eventBoxes)[3]"),
                violation<NotNull>("lightRotationEventBoxGroups[0].e(eventBoxes)[4]"),
                violation<CorrectType>("lightRotationEventBoxGroups[1].e(eventBoxes)"),
                violation<NotNull>("lightRotationEventBoxGroups[2].b(beat)"),
                violation<NotNull>("lightRotationEventBoxGroups[2].g(groupId)"),
                violation<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).f(type)"),
                violation<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).p(param0)"),
                violation<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).t(param1)"),
                violation<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).r(reversed)"),
                violation<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).c(chunks)"),
                violation<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).n(randomType)"),
                violation<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).s(seed)"),
                violation<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).l(limit)"),
                violation<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).d(alsoAffectsType)"),
                violation<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].w(beatDistributionParam)"),
                violation<In<Int>>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].d(beatDistributionParamType)"),
                violation<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].s(rotationDistributionParam)"),
                violation<In<Int>>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].t(rotationDistributionParamType)"),
                violation<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].a(axis)"),
                violation<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].r(flipRotation)"),
                violation<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].b(brightnessDistributionShouldAffectFirstBaseEvent)"),
                violation<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].l(lightRotationBaseDataList)[0].b(beat)"),
                violation<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].l(lightRotationBaseDataList)[0].p(usePreviousEventRotationValue)"),
                violation<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].l(lightRotationBaseDataList)[0].e(easeType)"),
                violation<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].l(lightRotationBaseDataList)[0].l(loopsCount)"),
                violation<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].l(lightRotationBaseDataList)[0].r(rotation)"),
                violation<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[0].l(lightRotationBaseDataList)[0].o(rotationDirection)"),
                violation<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[1].f(indexFilter)"),
                violation<NotNull>("lightRotationEventBoxGroups[2].e(eventBoxes)[1].l(lightRotationBaseDataList)"),
                violation<NotNull>("lightRotationEventBoxGroups[3].e(eventBoxes)"),
                violation<CorrectType>("lightRotationEventBoxGroups[4]"),
                violation<NotNull>("lightRotationEventBoxGroups[5]"),

                violation<CorrectType>("lightTranslationEventBoxGroups[0].b(beat)"),
                violation<CorrectType>("lightTranslationEventBoxGroups[0].g(groupId)"),
                violation<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).f(type)"),
                violation<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).p(param0)"),
                violation<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).t(param1)"),
                violation<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).r(reversed)"),
                violation<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).c(chunks)"),
                violation<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).n(randomType)"),
                violation<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).s(seed)"),
                violation<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).l(limit)"),
                violation<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).d(alsoAffectsType)"),
                violation<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].s(gapDistributionParam)"),
                violation<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].t(gapDistributionParamType)"),
                violation<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].a(axis)"),
                violation<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].r(flipTranslation)"),
                violation<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].b(gapDistributionShouldAffectFirstBaseEvent)"),
                violation<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].i(gapDistributionEaseType)"),
                violation<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].l(lightTranslationBaseDataList)[0].b(beat)"),
                violation<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].l(lightTranslationBaseDataList)[0].p(usePreviousEventTranslationValue)"),
                violation<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].l(lightTranslationBaseDataList)[0].e(easeType)"),
                violation<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].l(lightTranslationBaseDataList)[0].t(translation)"),
                violation<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].l(lightTranslationBaseDataList)[2]"),
                violation<NotNull>("lightTranslationEventBoxGroups[0].e(eventBoxes)[0].l(lightTranslationBaseDataList)[3]"),
                violation<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[1].f(indexFilter)"),
                violation<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[1].l(lightTranslationBaseDataList)"),
                violation<NodePresent>("lightTranslationEventBoxGroups[0].e(eventBoxes)[2].f(indexFilter)"),
                violation<CorrectType>("lightTranslationEventBoxGroups[0].e(eventBoxes)[3]"),
                violation<NotNull>("lightTranslationEventBoxGroups[0].e(eventBoxes)[4]"),
                violation<CorrectType>("lightTranslationEventBoxGroups[1].e(eventBoxes)"),
                violation<NotNull>("lightTranslationEventBoxGroups[2].b(beat)"),
                violation<NotNull>("lightTranslationEventBoxGroups[2].g(groupId)"),
                violation<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).f(type)"),
                violation<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).p(param0)"),
                violation<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).t(param1)"),
                violation<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).r(reversed)"),
                violation<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).c(chunks)"),
                violation<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).n(randomType)"),
                violation<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).s(seed)"),
                violation<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).l(limit)"),
                violation<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).d(alsoAffectsType)"),
                violation<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[0].s(gapDistributionParam)"),
                violation<In<Int>>("lightTranslationEventBoxGroups[2].e(eventBoxes)[0].t(gapDistributionParamType)"),
                violation<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[0].a(axis)"),
                violation<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[0].r(flipTranslation)"),
                violation<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[0].b(gapDistributionShouldAffectFirstBaseEvent)"),
                violation<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[0].i(gapDistributionEaseType)"),
                violation<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[0].l(lightTranslationBaseDataList)[0].b(beat)"),
                violation<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[0].l(lightTranslationBaseDataList)[0].p(usePreviousEventTranslationValue)"),
                violation<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[0].l(lightTranslationBaseDataList)[0].e(easeType)"),
                violation<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[0].l(lightTranslationBaseDataList)[0].t(translation)"),
                violation<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[1].f(indexFilter)"),
                violation<NotNull>("lightTranslationEventBoxGroups[2].e(eventBoxes)[1].l(lightTranslationBaseDataList)"),
                violation<NotNull>("lightTranslationEventBoxGroups[3].e(eventBoxes)"),
                violation<CorrectType>("lightTranslationEventBoxGroups[4]"),
                violation<NotNull>("lightTranslationEventBoxGroups[5]"),

                violation<CorrectType>("vfxEventBoxGroups[0].b(beat)"),
                violation<CorrectType>("vfxEventBoxGroups[0].g(groupId)"),
                violation<CorrectType>("vfxEventBoxGroups[0].t(type)"),
                violation<CorrectType>("vfxEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).f(type)"),
                violation<CorrectType>("vfxEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).p(param0)"),
                violation<CorrectType>("vfxEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).t(param1)"),
                violation<CorrectType>("vfxEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).r(reversed)"),
                violation<CorrectType>("vfxEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).c(chunks)"),
                violation<CorrectType>("vfxEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).n(randomType)"),
                violation<CorrectType>("vfxEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).s(seed)"),
                violation<CorrectType>("vfxEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).l(limit)"),
                violation<CorrectType>("vfxEventBoxGroups[0].e(eventBoxes)[0].f(indexFilter).d(alsoAffectsType)"),
                violation<CorrectType>("vfxEventBoxGroups[0].e(eventBoxes)[0].w(beatDistributionParam)"),
                violation<CorrectType>("vfxEventBoxGroups[0].e(eventBoxes)[0].d(beatDistributionParamType)"),
                violation<CorrectType>("vfxEventBoxGroups[0].e(eventBoxes)[0].s(vfxDistributionParam)"),
                violation<CorrectType>("vfxEventBoxGroups[0].e(eventBoxes)[0].t(vfxDistributionParamType)"),
                violation<CorrectType>("vfxEventBoxGroups[0].e(eventBoxes)[0].i(vfxDistributionEaseType)"),
                violation<CorrectType>("vfxEventBoxGroups[0].e(eventBoxes)[0].b(vfxDistributionShouldAffectFirstBaseEvent)"),
                violation<CorrectType>("vfxEventBoxGroups[0].e(eventBoxes)[0].l(vfxBaseDataList)[0]"),
                violation<NotNull>("vfxEventBoxGroups[0].e(eventBoxes)[0].l(vfxBaseDataList)[1]"),
                violation<CorrectType>("vfxEventBoxGroups[0].e(eventBoxes)[1].f(indexFilter)"),
                violation<CorrectType>("vfxEventBoxGroups[0].e(eventBoxes)[1].l(vfxBaseDataList)"),
                violation<NodePresent>("vfxEventBoxGroups[0].e(eventBoxes)[2].f(indexFilter)"),
                violation<CorrectType>("vfxEventBoxGroups[0].e(eventBoxes)[3]"),
                violation<NotNull>("vfxEventBoxGroups[0].e(eventBoxes)[4]"),
                violation<CorrectType>("vfxEventBoxGroups[1].e(eventBoxes)"),
                violation<NotNull>("vfxEventBoxGroups[2].b(beat)"),
                violation<NotNull>("vfxEventBoxGroups[2].g(groupId)"),
                violation<In<Int>>("vfxEventBoxGroups[2].t(type)"),
                violation<NotNull>("vfxEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).f(type)"),
                violation<NotNull>("vfxEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).p(param0)"),
                violation<NotNull>("vfxEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).t(param1)"),
                violation<NotNull>("vfxEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).r(reversed)"),
                violation<NotNull>("vfxEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).c(chunks)"),
                violation<NotNull>("vfxEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).n(randomType)"),
                violation<NotNull>("vfxEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).s(seed)"),
                violation<NotNull>("vfxEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).l(limit)"),
                violation<NotNull>("vfxEventBoxGroups[2].e(eventBoxes)[0].f(indexFilter).d(alsoAffectsType)"),
                violation<NotNull>("vfxEventBoxGroups[2].e(eventBoxes)[0].w(beatDistributionParam)"),
                violation<In<Int>>("vfxEventBoxGroups[2].e(eventBoxes)[0].d(beatDistributionParamType)"),
                violation<NotNull>("vfxEventBoxGroups[2].e(eventBoxes)[0].s(vfxDistributionParam)"),
                violation<In<Int>>("vfxEventBoxGroups[2].e(eventBoxes)[0].t(vfxDistributionParamType)"),
                violation<NotNull>("vfxEventBoxGroups[2].e(eventBoxes)[0].i(vfxDistributionEaseType)"),
                violation<NotNull>("vfxEventBoxGroups[2].e(eventBoxes)[0].b(vfxDistributionShouldAffectFirstBaseEvent)"),
                violation<NotNull>("vfxEventBoxGroups[2].e(eventBoxes)[0].l(vfxBaseDataList)"),
                violation<NotNull>("vfxEventBoxGroups[2].e(eventBoxes)[1].f(indexFilter)"),
                violation<NotNull>("vfxEventBoxGroups[3].e(eventBoxes)"),
                violation<CorrectType>("vfxEventBoxGroups[5]"),
                violation<NotNull>("vfxEventBoxGroups[6]"),

                violation<CorrectType>("_fxEventsCollection._il(intEventsList)[0].b(beat)"),
                violation<CorrectType>("_fxEventsCollection._il(intEventsList)[0].p(usePreviousEventValue)"),
                violation<CorrectType>("_fxEventsCollection._il(intEventsList)[0].v(value)"),
                violation<NotNull>("_fxEventsCollection._il(intEventsList)[1].b(beat)"),
                violation<NotNull>("_fxEventsCollection._il(intEventsList)[1].p(usePreviousEventValue)"),
                violation<NotNull>("_fxEventsCollection._il(intEventsList)[1].v(value)"),
                violation<CorrectType>("_fxEventsCollection._il(intEventsList)[3]"),
                violation<NotNull>("_fxEventsCollection._il(intEventsList)[4]"),

                violation<CorrectType>("_fxEventsCollection._fl(floatEventsList)[0].b(beat)"),
                violation<CorrectType>("_fxEventsCollection._fl(floatEventsList)[0].p(usePreviousEventValue)"),
                violation<CorrectType>("_fxEventsCollection._fl(floatEventsList)[0].v(value)"),
                violation<CorrectType>("_fxEventsCollection._fl(floatEventsList)[0].i(easeType)"),
                violation<NotNull>("_fxEventsCollection._fl(floatEventsList)[1].b(beat)"),
                violation<NotNull>("_fxEventsCollection._fl(floatEventsList)[1].p(usePreviousEventValue)"),
                violation<NotNull>("_fxEventsCollection._fl(floatEventsList)[1].v(value)"),
                violation<NotNull>("_fxEventsCollection._fl(floatEventsList)[1].i(easeType)"),
                violation<CorrectType>("_fxEventsCollection._fl(floatEventsList)[3]"),
                violation<NotNull>("_fxEventsCollection._fl(floatEventsList)[4]"),

                violation<CorrectType>("useNormalEventsAsCompatibleEvents")
            ),
            ex.constraintViolations
        )
    }
}
