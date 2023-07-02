package com.aureusapps.android.extensions

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.nio.ByteBuffer

class ByteBufferExtensionsTest {

    private lateinit var buffer: ByteBuffer

    @Before
    fun prepareBuffer() {
        buffer = ByteBuffer.allocate(20)
    }

    @Test
    fun test_nextByte() {
        buffer.put(10)
        buffer.position(0)
        val nextByte = buffer.nextByte()
        assertEquals(10.toByte(), nextByte)
        assertEquals(1, buffer.position())
    }

    @Test
    fun test_nextBytes() {
        buffer.put(10)
        buffer.put(10)
        buffer.position(0)
        val nextBytes = buffer.nextBytes(2)
        assertEquals(0b0000_1010_0000_1010, nextBytes)
        assertEquals(2, buffer.position())
    }

    @Test
    fun test_nextInt() {
        buffer.putInt(10)
        buffer.position(0)
        val nextInt = buffer.nextInt()
        assertEquals(10, nextInt)
        assertEquals(4, buffer.position())
    }

    @Test
    fun test_nextString() {
        buffer.put("HELLO THERE".toByteArray(Charsets.US_ASCII))
        buffer.position(0)
        val nextString = buffer.nextString(5, Charsets.US_ASCII)
        assertEquals("HELLO", nextString)
    }

    @Test
    fun test_skipBytes() {
        buffer.position(2)
        buffer.skipBytes(4)
        assertEquals(6, buffer.position())
    }

    @Test
    fun test_putString() {
        buffer.putString("HELLO THERE")
        val oLetter = buffer.get(4)
        assertEquals('O'.code, oLetter.toInt())
    }

}