package io.beatmaps.common.schema

import io.beatmaps.common.schema.SchemaCommon.validateFolder
import org.junit.Test
import kotlin.test.assertNull

class SchemaTest {
    @Test
    fun basic() {
        val ex = validateFolder("basic")
        assertNull(ex)
    }
}
