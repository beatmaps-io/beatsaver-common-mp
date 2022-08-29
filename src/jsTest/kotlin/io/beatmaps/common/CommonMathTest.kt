package io.beatmaps.common

import kotlin.test.Test
import kotlin.test.assertEquals

class CommonMathTest {
    @Test
    fun fixedDouble() {
        assertEquals(1.23, 1.234.fixed(2))
        assertEquals(1.2, 1.234.fixed(1))
        assertEquals(1.234, 1.234.fixed(3))
        assertEquals(1.234, 1.234.fixed(4))
        assertEquals(1.0, 1.234.fixed(0))
    }

    @Test
    fun fixedStrDouble() {
        assertEquals("1.23", 1.234.fixedStr(2))
        assertEquals("1.2", 1.234.fixedStr(1))
        assertEquals("1.234", 1.234.fixedStr(3))
        assertEquals("1.2340", 1.234.fixedStr(4))
        assertEquals("1", 1.234.fixedStr(0))
    }

    @Test
    fun fixedFloat() {
        assertEquals(1.23f, 1.234f.fixed(2))
        assertEquals(1.2f, 1.234f.fixed(1))
        assertEquals(1.234f, 1.234f.fixed(3))
        assertEquals(1.234f, 1.234f.fixed(4))
        assertEquals(1.0f, 1.234f.fixed(0))
    }

    @Test
    fun fixedStrFloat() {
        assertEquals("1.23", 1.234f.fixedStr(2))
        assertEquals("1.2", 1.234f.fixedStr(1))
        assertEquals("1.234", 1.234f.fixedStr(3))
        assertEquals("1.2340", 1.234f.fixedStr(4))
        assertEquals("1", 1.234f.fixedStr(0))
    }

    @Test
    fun powInt() {
        assertEquals(10_000_000_000, 10.pow(10))
        assertEquals(1024, 2.pow(10))
    }
}
