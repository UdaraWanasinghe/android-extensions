package com.aureusapps.android.extensions

import org.junit.Assert.assertEquals
import org.junit.Test

class ByteExtensionsTest {

    @Test
    fun test_getBits() {
        val value = 0b1001_0000.toByte()
        val bits = value.getBits(7)
        assertEquals(0b1000_0000, bits)
    }

    @Test
    fun test_getBitRange() {
        val value = 0b1010_1010.toByte()
        val bits = value.getBitRange(5..7)
        assertEquals(0b1010_0000, bits)
    }

}