package io.beatmaps.common.schema

import io.beatmaps.common.beatsaber.NodeNotPresent
import io.beatmaps.common.beatsaber.NodePresent
import io.beatmaps.common.schema.SchemaCommon.validateFolder
import io.beatmaps.common.schema.SchemaCommon.violation
import org.junit.Test
import kotlin.test.assertContentEquals
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
                violation<NodePresent>("basicEventTypesWithKeywords"),
                violation<NodePresent>("useNormalEventsAsCompatibleEvents")
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

        assertContentEquals(
            listOf<Any>(
                violation<NodeNotPresent>("lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.chunks"),
                violation<NodeNotPresent>("lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.randomType"),
                violation<NodeNotPresent>("lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.seed"),
                violation<NodeNotPresent>("lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.limit"),
                violation<NodeNotPresent>("lightColorEventBoxGroups[0].eventBoxes[0].indexFilter.alsoAffectsType"),
                violation<NodeNotPresent>("lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.chunks"),
                violation<NodeNotPresent>("lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.randomType"),
                violation<NodeNotPresent>("lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.seed"),
                violation<NodeNotPresent>("lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.limit"),
                violation<NodeNotPresent>("lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.alsoAffectsType"),
                violation<NodeNotPresent>("lightTranslationEventBoxGroups")
            ),
            ex.constraintViolations
        )
    }
}
