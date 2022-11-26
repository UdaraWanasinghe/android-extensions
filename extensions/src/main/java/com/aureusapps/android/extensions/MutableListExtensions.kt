package com.aureusapps.android.extensions

fun <E> MutableList<E>.move(from: Int, toIndex: Int) {
    if (from >= size || from < 0)
        throw IndexOutOfBoundsException(outOfBoundsMsg(from, size))
    if (toIndex >= size || toIndex < 0)
        throw IndexOutOfBoundsException(outOfBoundsMsg(toIndex, size))
    if (from == toIndex) return
    if (from < toIndex) {
        var f = from
        val item = set(f, get(f + 1))
        f++
        while (f < toIndex) {
            set(f, get(f + 1))
            f++
        }
        set(toIndex, item)
    } else {
        var f = from
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