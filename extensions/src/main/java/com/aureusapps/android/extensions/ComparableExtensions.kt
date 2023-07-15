package com.aureusapps.android.extensions

/**
 * Clamps the value to the specified range defined by [min] and [max].
 *
 * @param min The minimum value of the range.
 * @param max The maximum value of the range.
 * @return The clamped value. If the value is less than [min], [min] is returned. If the value is greater than [max], [max] is returned. Otherwise, the original value is returned.
 */
fun <T : Comparable<T>> T.clamp(min: T, max: T): T {
    return when {
        this < min -> min
        this > max -> max
        else -> this
    }
}