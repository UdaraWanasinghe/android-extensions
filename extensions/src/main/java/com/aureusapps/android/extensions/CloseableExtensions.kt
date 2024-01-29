package com.aureusapps.android.extensions

import com.aureusapps.android.extensions.utils.Logger
import java.io.Closeable

fun Closeable.closeQuietly() {
    try {
        close()
    } catch (e: Exception) {
        Logger.e(e.message ?: e::class.simpleName ?: "Unknown error")
    }
}