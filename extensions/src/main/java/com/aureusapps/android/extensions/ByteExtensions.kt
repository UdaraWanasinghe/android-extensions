package com.aureusapps.android.extensions

/**
 * Retrieves the specified bits from the byte.
 *
 * @param indexes The indexes of the bits to retrieve.
 * @return An integer representing the specified bits.
 * @throws IndexOutOfBoundsException if any of the specified indexes are out of range (0-7).
 */
fun Byte.getBits(vararg indexes: Int): Int {
    var v = 0
    for (i in indexes) {
        if (i < 0 || i > 7) {
            throw IndexOutOfBoundsException("Out of byte range [$i]")
        }
        v = v or ((1 shl i) and toInt())
    }
    return v
}

/**
 * Retrieves the bits within the specified range from the byte.
 *
 * @param range The range of bit indexes to retrieve.
 * @return An integer representing the bits within the specified range.
 * @throws IndexOutOfBoundsException if the start or end index is out of range (0-7).
 */
fun Byte.getBitRange(range: IntRange): Int {
    if (range.first < 0 || range.last > 7) {
        throw IndexOutOfBoundsException("Out of byte range [${range.first}-${range.last}]")
    }
    var v = 0
    for (i in range) {
        v = v or ((1 shl i) and toInt())
    }
    return v
}