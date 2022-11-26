package com.aureusapps.android.extensions

fun <E> ArrayList<E>.move(from: Int, to: Int) {
    if (from >= size || from < 0)
        throw IndexOutOfBoundsException(outOfBoundsMsg(from, size))
    if (to >= size || to < 0)
        throw IndexOutOfBoundsException(outOfBoundsMsg(to, size))
    if (from == to) return
    if (from < to) {
        var f = from
        val item = set(f, get(f + 1))
        f++
        while (f < to) {
            set(f, get(f + 1))
            f++
        }
        set(to, item)
    } else {
        var f = from
        val item = set(f, get(f - 1))
        f--
        while (f > to) {
            set(f, get(f - 1))
            f--
        }
        set(to, item)
    }
}

private fun outOfBoundsMsg(index: Int, size: Int): String {
    return "Index: $index, Size: $size"
}