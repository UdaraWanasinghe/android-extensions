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

/**
 * Extension property to convert an integer representing kilobytes to bytes.
 *
 * Usage:
 * val fileSizeInBytes = 5.KB // 5 KB = 5120 bytes
 */
val Int.KB: Int
    get() = this * 1024

/**
 * Extension property to converts an integer representing megabytes to bytes.
 *
 * Usage:
 * val fileSizeInBytes = 5.MB // 5 MB = 5242880 bytes
 */
val Int.MB: Int
    get() = this * 1024 * 1024

/**
 * Extension property to convert an integer representing gigabytes to bytes.
 *
 * Usage:
 * val fileSizeInBytes = 2.GB // 2 GB = 2147483648 bytes
 */
val Int.GB: Long
    get() = this * 1024L * 1024L * 1024L

/**
 * Extension property to convert an integer representing terabytes to bytes.
 *
 * Usage:
 * val fileSizeInBytes = 1.TB // 1 TB = 1099511627776 bytes
 */
val Int.TB: Long
    get() = this.toLong() * 1024 * 1024 * 1024 * 1024

/**
 * Extension property to convert an integer representing minutes to milliseconds.
 *
 * Usage:
 * val durationInMilliseconds = 5.minutes // 5 minutes = 300000 milliseconds
 */
val Int.minutes: Long
    get() = this * 60 * 1000L

/**
 * Extension property to convert an integer representing hours to milliseconds.
 *
 * Usage:
 * val durationInMilliseconds = 2.hours // 2 hours = 7200000 milliseconds
 */
val Int.hours: Long
    get() = this * 60 * 60 * 1000L

/**
 * Extension property to convert an integer representing days to milliseconds.
 *
 * Usage:
 * val durationInMilliseconds = 1.days // 1 day = 86400000 milliseconds
 */
val Int.days: Long
    get() = this * 24 * 60 * 60 * 1000L