package com.aureusapps.android.extensions

import org.junit.Assert.assertEquals
import org.junit.Test

class ByteExtensionsUnitTest {

    @Test
    fun test_getBits() {
        val value = 0b10010000.toByte()
        val bits = value.getBits(7)
        assertEquals(0b10000000, bits)
    }

    @Test
    fun test_getBitRange() {
        val value = 0b10101010.toByte()
        val bits = value.getBitRange(5..7)
        assertEquals(0b10100000, bits)
    }

}