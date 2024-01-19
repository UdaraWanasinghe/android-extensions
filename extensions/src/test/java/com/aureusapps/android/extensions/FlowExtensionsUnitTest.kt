package com.aureusapps.android.extensions

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FlowExtensionsUnitTest {

    @Test
    fun testScanNotNullWithInitial() = runTest {
        val flow = flowOf(1, 2, 3)
        val result = flow.scanNotNull(emptyList<Int>()) { acc, value ->
            if (value == 2) null else acc + value
        }.toList()
        assertEquals(listOf(emptyList(), listOf(1), listOf(1, 3)), result)
    }

    @Test
    fun testScanTransformedWithInitial() = runTest {
        val flow = flowOf(1, 2, 3)
        val result = flow.scanTransform(0) { acc, value -> emit(acc); emit(acc + value) }.toList()
        assertEquals(listOf(0, 0, 1, 1, 3, 3, 6), result)
    }

    @Test
    fun testOnFirst() = runTest {
        val flow = flowOf(1, 2, 3)
        val first = mutableListOf<Int>()
        val second = mutableListOf<Int>()
        flow
            .onFirst {
                first.add(it)
            }
            .collect {
                second.add(it)
            }
        assertEquals(listOf(1), first)
        assertEquals(listOf(2, 3), second)
    }

}