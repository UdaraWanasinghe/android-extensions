package com.aureusapps.android.extensions

val Int.opaque get() = (0xFF shl 24) or (0x00FFFFFF and this)

fun Int.withAlpha(alpha: Int) = (alpha shl 24) or (0x00FFFFFF and this)

/**
 * Retrieves the specified bits from the integer.
 *
 * @param indexes The indexes of the bits to retrieve.
 * @return An integer representing the specified bits.
 * @throws IndexOutOfBoundsException if any of the specified indexes are out of range (0-31).
 */
fun Int.getBits(vararg indexes: Int): Int {
    var v = 0
    for (i in indexes) {
        if (i < 0 || i > 31) {
            throw IndexOutOfBoundsException("Out of integer range [$i]")
        }
        v = v or ((1 shl i) and this)
    }
    return v
}

/**
 * Retrieves the bits within the specified range from the integer.
 *
 * @param range The range of bit indexes to retrieve.
 * @return An integer representing the bits within the specified range.
 * @throws IndexOutOfBoundsException if the start or end index is out of range (0-31).
 */
fun Int.getBitRange(range: IntRange): Int {
    if (range.first < 0 || range.last > 31) {
        throw IndexOutOfBoundsException("Out of integer range [${range.first}-${range.last}")
    }
    var v = 0
    for (i in range) {
        v = v or ((1 shl i) and this)
    }
    return v
}