package io.beatmaps.common.schema

import io.beatmaps.common.schema.SchemaCommon.validateFolder
import io.beatmaps.common.schema.SchemaCommon.violation
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SchemaTest32 {
    @Test
    fun schema3_2() {
        val ex = validateFolder("3_2")
        assertNull(ex)
    }

    @Test
    fun schema3_2as3_0() {
        val ex = validateFolder("3_2as3_0")
        assertNotNull(ex)

        assertEquals(10, ex.constraintViolations.size)
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
                violation("lightRotationEventBoxGroups[0].eventBoxes[0].indexFilter.alsoAffectsType", 0)
            ),
            ex.constraintViolations
        )
    }
}
