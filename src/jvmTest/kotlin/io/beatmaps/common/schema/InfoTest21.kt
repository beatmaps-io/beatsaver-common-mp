package io.beatmaps.common.schema

import io.beatmaps.common.schema.SchemaCommon.validateFolder
import org.junit.Test
import kotlin.test.assertNull

class InfoTest21 {
    @Test
    fun basic() {
        val ex = validateFolder("info/2_1/basic")
        assertNull(ex)
    }
}
