package com.aureusapps.android.extensions

fun <T> Collection<T>.copyOf(): Collection<T> {
    return mutableListOf<T>().also { it.addAll(this) }
}

fun <T> MutableList<T>.copyOf(): MutableList<T> {
    return mutableListOf<T>().also { it.addAll(this) }
}

/**
 * Accumulates value starting with [initial] value and applying [operation] from left to right to current accumulator value and each element until value returned by operation is not null.
 *
 * @param initial The initial accumulator value.
 * @param operation Function that takes current accumulator value and an element, and calculates the next accumulator value
 *
 * Example Usage:
 * ```kotlin
 * val list = listOf(2, 4, 6, 8, 9, 11)
 *
 * val result = list.foldUntilNotNull(0) { acc, element ->
 *     if (element % 2 == 0) acc + element else null
 * }
 *
 * println(result) // Output: 20 (Sum of even numbers until the first odd number)
 * ```
 */
inline fun <T, R> Iterable<T>.foldUntilNotNull(initial: R, operation: (acc: R, T) -> R?): R {
    var accumulator = initial
    for (element in this) accumulator = operation(accumulator, element) ?: break
    return accumulator
}