package com.aureusapps.android.extensions

fun <E> MutableList<E>.move(fromIndex: Int, toIndex: Int) {
    if (fromIndex >= size || fromIndex < 0)
        throw IndexOutOfBoundsException(outOfBoundsMsg(fromIndex, size))
    if (toIndex >= size || toIndex < 0)
        throw IndexOutOfBoundsException(outOfBoundsMsg(toIndex, size))
    if (fromIndex == toIndex) return
    if (fromIndex < toIndex) {
        var f = fromIndex
        val item = set(f, get(f + 1))
        f++
        while (f < toIndex) {
            set(f, get(f + 1))
            f++
        }
        set(toIndex, item)
    } else {
        var f = fromIndex
        val item = set(f, get(f - 1))
        f--
        while (f > toIndex) {
            set(f, get(f - 1))
            f--
        }
        set(toIndex, item)
    }
}

private fun outOfBoundsMsg(index: Int, size: Int): String {
    return "Index: $index, Size: $size"
}