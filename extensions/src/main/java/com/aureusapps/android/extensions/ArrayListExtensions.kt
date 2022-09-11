package com.aureusapps.android.extensions

import java.util.*

fun <E> ArrayList<E>.move(fromIndex: Int, toIndex: Int) {
    if (fromIndex >= size || fromIndex < 0)
        throw IndexOutOfBoundsException(outOfBoundsMsg(fromIndex, size))
    if (toIndex >= size || toIndex < 0)
        throw IndexOutOfBoundsException(outOfBoundsMsg(toIndex, size))
    if (fromIndex == toIndex) return
    if (fromIndex < toIndex) {
        var from = fromIndex
        val item = set(from, get(from + 1))
        from++
        while (from < toIndex) {
            set(from, get(from + 1))
            from++
        }
        set(toIndex, item)
    } else {
        var from = fromIndex
        val item = set(from, get(from - 1))
        from--
        while (from > toIndex) {
            set(from, get(from - 1))
            from--
        }
        set(toIndex, item)
    }
}

private fun outOfBoundsMsg(index: Int, size: Int): String {
    return "Index: $index, Size: $size"
}