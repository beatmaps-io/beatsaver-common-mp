package io.beatmaps.common.schema

import io.beatmaps.common.schema.SchemaCommon.validateFolder
import org.junit.Test
import kotlin.test.assertNull

class InfoTest40 {
    @Test
    fun basic() {
        val ex = validateFolder("info/4_0/basic")
        assertNull(ex)
    }
}
