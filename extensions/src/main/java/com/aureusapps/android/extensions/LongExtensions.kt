package com.aureusapps.android.extensions

/**
 * Extension property to converts a long value representing kilobytes to bytes.
 *
 * Usage:
 * val fileSizeInBytes = 5L.KB // 5 KB = 5120 bytes
 */
val Long.KB: Long
    get() = this * 1024L

/**
 * Extension property to converts a long value representing megabytes to bytes.
 *
 * Usage:
 * val fileSizeInBytes = 5L.MB // 5 MB = 5242880 bytes
 */
val Long.MB: Long
    get() = this * 1024L * 1024L