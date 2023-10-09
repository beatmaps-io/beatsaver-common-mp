package io.beatmaps.common.schema

import io.beatmaps.common.OptionalProperty
import io.beatmaps.common.beatsaber.BSIndexFilter
import io.beatmaps.common.beatsaber.BSLightTranslationEventBox
import io.beatmaps.common.beatsaber.BSLightTranslationEventBoxGroup
import io.beatmaps.common.beatsaber.LightTranslationBaseData
import io.beatmaps.common.schema.SchemaCommon.validateFolder
import io.beatmaps.common.schema.SchemaCommon.violation
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SchemaTest32 {
    @Test
    fun basic() {
        val ex = validateFolder("3_2/basic")
        assertNull(ex)
    }

    @Test
    fun missing() {
        val ex = validateFolder("3_2/missing")
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
                violation("basicEventTypesWithKeywords"),
                violation("useNormalEventsAsCompatibleEvents")
            ),
            ex.constraintViolations
        )
    }

    @Test
    fun schema() {
        val ex = validateFolder("3_2/bullet")
        assertNull(ex)
    }

    @Test
    fun schemaAs3_0() {
        val ex = validateFolder("3_2/as3_0")
        assertNotNull(ex)

        assertEquals(
            setOf(
                violation("lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.chunks", 0),
                violation("lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.randomType", 0),
                violation("lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.seed", 0),
                violation("lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.limit", 0.0f),
                violation("lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.alsoAffectsType", 0),
                violation("lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.chunks", 0),
                violation("lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.randomType", 0),
                violation("lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.seed", 0),
                violation("lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.limit", 0.0f),
                violation("lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.alsoAffectsType", 0),
                violation("lightTranslationEventBoxGroups", listOf<OptionalProperty<BSLightTranslationEventBoxGroup>>())
            ),
            ex.constraintViolations
        )
    }
}
