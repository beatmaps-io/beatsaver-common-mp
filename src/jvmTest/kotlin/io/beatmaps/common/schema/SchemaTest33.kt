package io.beatmaps.common.schema

import io.beatmaps.common.OptionalProperty
import io.beatmaps.common.beatsaber.BSFxEventsCollection
import io.beatmaps.common.beatsaber.BSVfxEventBoxGroup
import io.beatmaps.common.schema.SchemaCommon.validateFolder
import io.beatmaps.common.schema.SchemaCommon.violation
import io.beatmaps.common.schema.SchemaCommon.violationWrong
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SchemaTest33 {
    @Test
    fun schemaAs3_3() {
        val ex = validateFolder("3_2/as3_3")
        assertNotNull(ex)

        assertEquals(
            setOf(
                violation("vfxEventBoxGroups"),
                violation("_fxEventsCollection"),
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

        assertEquals(
            setOf(
                violationWrong("version"),
                violationWrong("bpmEvents"),
                violationWrong("rotationEvents"),
                violationWrong("colorNotes"),
                violationWrong("bombNotes"),
                violationWrong("obstacles"),
                violationWrong("sliders"),
                violationWrong("burstSliders"),
                violationWrong("waypoints"),
                violationWrong("basicBeatmapEvents"),
                violationWrong("colorBoostBeatmapEvents"),
                violationWrong("lightColorEventBoxGroups"),
                violationWrong("lightRotationEventBoxGroups"),
                violationWrong("lightTranslationEventBoxGroups"),
                violationWrong("vfxEventBoxGroups"),
                violationWrong("_fxEventsCollection"),
                violationWrong("basicEventTypesWithKeywords"),
                violationWrong("useNormalEventsAsCompatibleEvents"),
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

        assertEquals(
            setOf(
                violation("bpmEvents"),
                violation("rotationEvents"),
                violation("colorNotes"),
                violation("bombNotes"),
                violation("obstacles"),
                violation("sliders"),
                violation("burstSliders"),
                violation("waypoints"),
                violation("basicBeatmapEvents"),
                violation("colorBoostBeatmapEvents"),
                violation("lightColorEventBoxGroups"),
                violation("lightRotationEventBoxGroups"),
                violation("lightTranslationEventBoxGroups"),
                violation("vfxEventBoxGroups"),
                violation("_fxEventsCollection"),
                violation("basicEventTypesWithKeywords")
            ),
            ex.constraintViolations
        )
    }

    @Test
    fun schemaAs3_2() {
        val ex = validateFolder("3_3/as3_2")
        assertNotNull(ex)

        assertEquals(
            setOf(
                violation("bpmEvents[0].beat"),

                violation("colorNotes[0]._time"),
                violation("colorNotes[0].y"),
                violation("colorNotes[0].angleOffset"),
                violation("colorNotes[1].color"),
                violation("colorNotes[1].y"),
                violation("colorNotes[1].angleOffset"),
                violation("colorNotes[2].y"),
                violation("colorNotes[2].angleOffset"),
                violation("colorNotes[4].color"),
                violation("colorNotes[4].x"),
                violation("colorNotes[4].y"),
                violation("colorNotes[4].angleOffset"),
                violation("colorNotes[5].color"),
                violation("colorNotes[5].direction"),
                violation("colorNotes[5].x"),
                violation("colorNotes[5].y"),
                violation("colorNotes[5].angleOffset"),
                violation("colorNotes[6].angleOffset"),

                violation("bombNotes[0].beat"),
                violation("bombNotes[0].x"),
                violation("bombNotes[0].y"),
                violation("bombNotes[1].x"),
                violation("bombNotes[1].y"),

                violation("obstacles[0].x"),
                violation("obstacles[0].y"),
                violation("obstacles[1].y"),

                violation("sliders[0].color"),
                violation("sliders[0].x"),
                violation("sliders[0].y"),
                violation("sliders[0].tailX"),
                violation("sliders[0].tailY"),
                violation("sliders[0].tailCutDirection"),
                violation("sliders[0].sliderMidAnchorMode"),

                violation("burstSliders[0].tailX"),
                violation("burstSliders[0].tailY"),

                violation("basicBeatmapEvents[0].beat"),
                violation("basicBeatmapEvents[1].beat"),
                violation("basicBeatmapEvents[1].value"),
                violation("basicBeatmapEvents[3].value"),
                violation("basicBeatmapEvents[6].value"),
                violation("basicBeatmapEvents[9].value"),
                violation("basicBeatmapEvents[9].floatValue"),
                violation("basicBeatmapEvents[10].value"),
                violation("basicBeatmapEvents[10].floatValue"),
                violation("basicBeatmapEvents[11].floatValue"),
                violation("basicBeatmapEvents[12].floatValue"),
                violation("basicBeatmapEvents[13].floatValue"),

                violation("colorBoostBeatmapEvents[0].beat"),
                violation("colorBoostBeatmapEvents[0].boost"),
                violation("colorBoostBeatmapEvents[2].boost"),
                violation("colorBoostBeatmapEvents[4].boost"),

                violation("lightColorEventBoxGroups[0].beat"),
                violation("lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.param0"),
                violation("lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.param1"),
                violation("lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.reversed"),
                violation("lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.chunks"),
                violation("lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.randomType"),
                violation("lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.seed"),
                violation("lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.limit"),
                violation("lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.alsoAffectsType"),
                violation("lightColorEventBoxGroups[0].eventBoxes[0].beatDistributionParam"),
                violation("lightColorEventBoxGroups[0].eventBoxes[0].brightnessDistributionParam"),
                violation("lightColorEventBoxGroups[0].eventBoxes[0].brightnessDistributionShouldAffectFirstBaseEvent"),
                violation("lightColorEventBoxGroups[0].eventBoxes[0].lightColorBaseDataList[0].beat"),
                violation("lightColorEventBoxGroups[0].eventBoxes[0].lightColorBaseDataList[0].transitionType"),
                violation("lightColorEventBoxGroups[0].eventBoxes[0].lightColorBaseDataList[0].strobeFrequency"),
                violation("lightColorEventBoxGroups[0].eventBoxes[1].indexFilter.param1"),
                violation("lightColorEventBoxGroups[0].eventBoxes[1].indexFilter.reversed"),
                violation("lightColorEventBoxGroups[0].eventBoxes[1].indexFilter.chunks"),
                violation("lightColorEventBoxGroups[0].eventBoxes[1].indexFilter.randomType"),
                violation("lightColorEventBoxGroups[0].eventBoxes[1].indexFilter.seed"),
                violation("lightColorEventBoxGroups[0].eventBoxes[1].indexFilter.limit"),
                violation("lightColorEventBoxGroups[0].eventBoxes[1].indexFilter.alsoAffectsType"),
                violation("lightColorEventBoxGroups[0].eventBoxes[1].beatDistributionParam"),
                violation("lightColorEventBoxGroups[0].eventBoxes[1].brightnessDistributionParam"),
                violation("lightColorEventBoxGroups[0].eventBoxes[1].brightnessDistributionShouldAffectFirstBaseEvent"),
                violation("lightColorEventBoxGroups[0].eventBoxes[1].lightColorBaseDataList[0].transitionType"),
                violation("lightColorEventBoxGroups[0].eventBoxes[1].lightColorBaseDataList[0].strobeFrequency"),
                violation("lightColorEventBoxGroups[0].eventBoxes[1].lightColorBaseDataList[1].transitionType"),
                violation("lightColorEventBoxGroups[0].eventBoxes[1].lightColorBaseDataList[1].brightness"),
                violation("lightColorEventBoxGroups[0].eventBoxes[1].lightColorBaseDataList[1].strobeFrequency"),
                violation("lightColorEventBoxGroups[0].eventBoxes[2].indexFilter.param1"),
                violation("lightColorEventBoxGroups[0].eventBoxes[2].indexFilter.reversed"),
                violation("lightColorEventBoxGroups[0].eventBoxes[2].indexFilter.chunks"),
                violation("lightColorEventBoxGroups[0].eventBoxes[2].indexFilter.randomType"),
                violation("lightColorEventBoxGroups[0].eventBoxes[2].indexFilter.seed"),
                violation("lightColorEventBoxGroups[0].eventBoxes[2].indexFilter.limit"),
                violation("lightColorEventBoxGroups[0].eventBoxes[2].indexFilter.alsoAffectsType"),
                violation("lightColorEventBoxGroups[0].eventBoxes[2].beatDistributionParam"),
                violation("lightColorEventBoxGroups[0].eventBoxes[2].brightnessDistributionParam"),
                violation("lightColorEventBoxGroups[0].eventBoxes[2].brightnessDistributionShouldAffectFirstBaseEvent"),
                violation("lightColorEventBoxGroups[0].eventBoxes[2].lightColorBaseDataList[0].transitionType"),
                violation("lightColorEventBoxGroups[0].eventBoxes[2].lightColorBaseDataList[0].strobeFrequency"),
                violation("lightColorEventBoxGroups[0].eventBoxes[2].lightColorBaseDataList[1].strobeFrequency"),
                violation("lightColorEventBoxGroups[0].eventBoxes[3].indexFilter.param1"),
                violation("lightColorEventBoxGroups[0].eventBoxes[3].indexFilter.reversed"),
                violation("lightColorEventBoxGroups[0].eventBoxes[3].indexFilter.chunks"),
                violation("lightColorEventBoxGroups[0].eventBoxes[3].indexFilter.randomType"),
                violation("lightColorEventBoxGroups[0].eventBoxes[3].indexFilter.seed"),
                violation("lightColorEventBoxGroups[0].eventBoxes[3].indexFilter.limit"),
                violation("lightColorEventBoxGroups[0].eventBoxes[3].indexFilter.alsoAffectsType"),
                violation("lightColorEventBoxGroups[0].eventBoxes[3].beatDistributionParam"),
                violation("lightColorEventBoxGroups[0].eventBoxes[3].brightnessDistributionParam"),
                violation("lightColorEventBoxGroups[0].eventBoxes[3].brightnessDistributionShouldAffectFirstBaseEvent"),
                violation("lightColorEventBoxGroups[0].eventBoxes[3].lightColorBaseDataList[0].transitionType"),
                violation("lightColorEventBoxGroups[0].eventBoxes[3].lightColorBaseDataList[0].strobeFrequency"),

                violation("lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.param1"),
                violation("lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.reversed"),
                violation("lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.chunks"),
                violation("lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.randomType"),
                violation("lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.seed"),
                violation("lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.limit"),
                violation("lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.alsoAffectsType"),
                violation("lightRotationEventBoxGroups[0].eventBoxes[0].beatDistributionParam"),
                violation("lightRotationEventBoxGroups[0].eventBoxes[0].rotationDistributionParam"),
                violation("lightRotationEventBoxGroups[0].eventBoxes[0].flipRotation"),
                violation("lightRotationEventBoxGroups[0].eventBoxes[0].brightnessDistributionShouldAffectFirstBaseEvent"),
                violation("lightRotationEventBoxGroups[0].eventBoxes[0].lightRotationBaseDataList[0].beat"),
                violation("lightRotationEventBoxGroups[0].eventBoxes[0].lightRotationBaseDataList[0].usePreviousEventRotationValue"),
                violation("lightRotationEventBoxGroups[0].eventBoxes[0].lightRotationBaseDataList[0].loopsCount"),
                violation("lightRotationEventBoxGroups[0].eventBoxes[0].lightRotationBaseDataList[0].rotationDirection"),

                violation("lightTranslationEventBoxGroups[0].eventBoxes[0].indexFilter.param1"),
                violation("lightTranslationEventBoxGroups[0].eventBoxes[0].indexFilter.reversed"),
                violation("lightTranslationEventBoxGroups[0].eventBoxes[0].indexFilter.chunks"),
                violation("lightTranslationEventBoxGroups[0].eventBoxes[0].indexFilter.randomType"),
                violation("lightTranslationEventBoxGroups[0].eventBoxes[0].indexFilter.seed"),
                violation("lightTranslationEventBoxGroups[0].eventBoxes[0].indexFilter.limit"),
                violation("lightTranslationEventBoxGroups[0].eventBoxes[0].indexFilter.alsoAffectsType"),

                violation("vfxEventBoxGroups", listOf<BSVfxEventBoxGroup>()),
                violation("_fxEventsCollection", BSFxEventsCollection(OptionalProperty.Present(listOf()), OptionalProperty.Present(listOf()))),

                violation("useNormalEventsAsCompatibleEvents")
            ),
            ex.constraintViolations
        )
    }
}
