package io.beatmaps.common.schema

import io.beatmaps.common.beatsaber.CorrectType
import io.beatmaps.common.beatsaber.NodeNotPresent
import io.beatmaps.common.beatsaber.NodePresent
import io.beatmaps.common.schema.SchemaCommon.validateFolder
import io.beatmaps.common.schema.SchemaCommon.violation
import org.junit.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SchemaTest33 {
    @Test
    fun schemaAs3_3() {
        val ex = validateFolder("3_2/as3_3")
        assertNotNull(ex)

        assertContentEquals(
            listOf<Any>(
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
            listOf<Any>(
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
    fun missing() {
        val ex = validateFolder("3_3/missing")
        assertNotNull(ex)

        assertContentEquals(
            listOf<Any>(
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
            listOf<Any>(
                violation<NodePresent>("bpmEvents[0].beat"),

                violation<NodePresent>("colorNotes[0]._time"),
                violation<NodePresent>("colorNotes[0].y"),
                violation<NodePresent>("colorNotes[0].angleOffset"),
                violation<NodePresent>("colorNotes[1].color"),
                violation<NodePresent>("colorNotes[1].y"),
                violation<NodePresent>("colorNotes[1].angleOffset"),
                violation<NodePresent>("colorNotes[2].y"),
                violation<NodePresent>("colorNotes[2].angleOffset"),
                violation<NodePresent>("colorNotes[4].color"),
                violation<NodePresent>("colorNotes[4].x"),
                violation<NodePresent>("colorNotes[4].y"),
                violation<NodePresent>("colorNotes[4].angleOffset"),
                violation<NodePresent>("colorNotes[5].color"),
                violation<NodePresent>("colorNotes[5].direction"),
                violation<NodePresent>("colorNotes[5].x"),
                violation<NodePresent>("colorNotes[5].y"),
                violation<NodePresent>("colorNotes[5].angleOffset"),
                violation<NodePresent>("colorNotes[6].angleOffset"),

                violation<NodePresent>("bombNotes[0]._time"),
                violation<NodePresent>("bombNotes[0].x"),
                violation<NodePresent>("bombNotes[0].y"),
                violation<NodePresent>("bombNotes[1].x"),
                violation<NodePresent>("bombNotes[1].y"),

                violation<NodePresent>("obstacles[0].x"),
                violation<NodePresent>("obstacles[0].y"),
                violation<NodePresent>("obstacles[1].y"),

                violation<NodePresent>("sliders[0].color"),
                violation<NodePresent>("sliders[0].x"),
                violation<NodePresent>("sliders[0].y"),
                violation<NodePresent>("sliders[0].tailX"),
                violation<NodePresent>("sliders[0].tailY"),
                violation<NodePresent>("sliders[0].tailCutDirection"),
                violation<NodePresent>("sliders[0].sliderMidAnchorMode"),

                violation<NodePresent>("burstSliders[0].tailX"),
                violation<NodePresent>("burstSliders[0].tailY"),

                violation<NodePresent>("basicBeatmapEvents[0]._time"),
                violation<NodePresent>("basicBeatmapEvents[1]._time"),
                violation<NodePresent>("basicBeatmapEvents[1].value"),
                violation<NodePresent>("basicBeatmapEvents[3].value"),
                violation<NodePresent>("basicBeatmapEvents[6].value"),
                violation<NodePresent>("basicBeatmapEvents[9].value"),
                violation<NodePresent>("basicBeatmapEvents[9].floatValue"),
                violation<NodePresent>("basicBeatmapEvents[10].value"),
                violation<NodePresent>("basicBeatmapEvents[10].floatValue"),
                violation<NodePresent>("basicBeatmapEvents[11].floatValue"),
                violation<NodePresent>("basicBeatmapEvents[12].floatValue"),
                violation<NodePresent>("basicBeatmapEvents[13].floatValue"),

                violation<NodePresent>("colorBoostBeatmapEvents[0].beat"),
                violation<NodePresent>("colorBoostBeatmapEvents[0].boost"),
                violation<NodePresent>("colorBoostBeatmapEvents[2].boost"),
                violation<NodePresent>("colorBoostBeatmapEvents[4].boost"),

                violation<NodePresent>("lightColorEventBoxGroups[0].beat"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.param0"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.param1"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.reversed"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.chunks"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.randomType"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.seed"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.limit"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.alsoAffectsType"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[0].beatDistributionParam"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[0].brightnessDistributionParam"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[0].brightnessDistributionShouldAffectFirstBaseEvent"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[0].lightColorBaseDataList[0].beat"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[0].lightColorBaseDataList[0].transitionType"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[0].lightColorBaseDataList[0].strobeFrequency"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[1].indexFilter.param1"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[1].indexFilter.reversed"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[1].indexFilter.chunks"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[1].indexFilter.randomType"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[1].indexFilter.seed"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[1].indexFilter.limit"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[1].indexFilter.alsoAffectsType"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[1].beatDistributionParam"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[1].brightnessDistributionParam"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[1].brightnessDistributionShouldAffectFirstBaseEvent"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[1].lightColorBaseDataList[0].transitionType"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[1].lightColorBaseDataList[0].strobeFrequency"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[1].lightColorBaseDataList[1].transitionType"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[1].lightColorBaseDataList[1].brightness"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[1].lightColorBaseDataList[1].strobeFrequency"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[2].indexFilter.param1"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[2].indexFilter.reversed"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[2].indexFilter.chunks"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[2].indexFilter.randomType"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[2].indexFilter.seed"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[2].indexFilter.limit"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[2].indexFilter.alsoAffectsType"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[2].beatDistributionParam"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[2].brightnessDistributionParam"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[2].brightnessDistributionShouldAffectFirstBaseEvent"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[2].lightColorBaseDataList[0].transitionType"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[2].lightColorBaseDataList[0].strobeFrequency"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[2].lightColorBaseDataList[1].strobeFrequency"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[3].indexFilter.param1"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[3].indexFilter.reversed"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[3].indexFilter.chunks"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[3].indexFilter.randomType"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[3].indexFilter.seed"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[3].indexFilter.limit"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[3].indexFilter.alsoAffectsType"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[3].beatDistributionParam"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[3].brightnessDistributionParam"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[3].brightnessDistributionShouldAffectFirstBaseEvent"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[3].lightColorBaseDataList[0].transitionType"),
                violation<NodePresent>("lightColorEventBoxGroups[0].eventBoxes[3].lightColorBaseDataList[0].strobeFrequency"),

                violation<NodePresent>("lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.param1"),
                violation<NodePresent>("lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.reversed"),
                violation<NodePresent>("lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.chunks"),
                violation<NodePresent>("lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.randomType"),
                violation<NodePresent>("lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.seed"),
                violation<NodePresent>("lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.limit"),
                violation<NodePresent>("lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.alsoAffectsType"),
                violation<NodePresent>("lightRotationEventBoxGroups[0].eventBoxes[0].beatDistributionParam"),
                violation<NodePresent>("lightRotationEventBoxGroups[0].eventBoxes[0].rotationDistributionParam"),
                violation<NodePresent>("lightRotationEventBoxGroups[0].eventBoxes[0].flipRotation"),
                violation<NodePresent>("lightRotationEventBoxGroups[0].eventBoxes[0].brightnessDistributionShouldAffectFirstBaseEvent"),
                violation<NodePresent>("lightRotationEventBoxGroups[0].eventBoxes[0].lightRotationBaseDataList[0].beat"),
                violation<NodePresent>("lightRotationEventBoxGroups[0].eventBoxes[0].lightRotationBaseDataList[0].usePreviousEventRotationValue"),
                violation<NodePresent>("lightRotationEventBoxGroups[0].eventBoxes[0].lightRotationBaseDataList[0].loopsCount"),
                violation<NodePresent>("lightRotationEventBoxGroups[0].eventBoxes[0].lightRotationBaseDataList[0].rotationDirection"),

                violation<NodePresent>("lightTranslationEventBoxGroups[0].eventBoxes[0].indexFilter.param1"),
                violation<NodePresent>("lightTranslationEventBoxGroups[0].eventBoxes[0].indexFilter.reversed"),
                violation<NodePresent>("lightTranslationEventBoxGroups[0].eventBoxes[0].indexFilter.chunks"),
                violation<NodePresent>("lightTranslationEventBoxGroups[0].eventBoxes[0].indexFilter.randomType"),
                violation<NodePresent>("lightTranslationEventBoxGroups[0].eventBoxes[0].indexFilter.seed"),
                violation<NodePresent>("lightTranslationEventBoxGroups[0].eventBoxes[0].indexFilter.limit"),
                violation<NodePresent>("lightTranslationEventBoxGroups[0].eventBoxes[0].indexFilter.alsoAffectsType"),

                violation<NodeNotPresent>("vfxEventBoxGroups"),
                violation<NodeNotPresent>("_fxEventsCollection"),

                violation<NodePresent>("useNormalEventsAsCompatibleEvents")
            ),
            ex.constraintViolations
        )
    }
}
