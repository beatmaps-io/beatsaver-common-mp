package io.beatmaps.common.schema

import io.beatmaps.common.beatsaber.NodeNotPresent
import io.beatmaps.common.schema.SchemaCommon.infoViolation
import io.beatmaps.common.schema.SchemaCommon.validateFolder
import org.junit.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class InfoTest401 {
    @Test
    fun basic() {
        val ex = validateFolder("info/4_0_1/basic")
        assertNull(ex)
    }

    @Test
    fun with4_0() {
        val ex = validateFolder("info/4_0_1/with4_0")
        assertNotNull(ex)

        assertContentEquals(
            listOf(
                infoViolation<NodeNotPresent>("colorSchemes[0].useOverride")
            ),
            ex.constraintViolations
        )
    }
}
