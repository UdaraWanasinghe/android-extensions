package com.aureusapps.android.extensions

import org.junit.Assert.assertEquals
import org.junit.Test

class IntExtensionsUnitTest {

    @Test
    fun test_getBits() {
        val value = 0b01100010_00010100_00000000_00000000
        val bits = value.getBits(30, 25)
        assertEquals(0b01000010_00000000_00000000_00000000, bits)
    }

    @Test
    fun test_getBitRange() {
        val value = 0b01100010_00010100_00000000_00000000
        val bits = value.getBitRange(25..30)
        assertEquals(0b01100010_00000000_00000000_00000000, bits)
    }

}