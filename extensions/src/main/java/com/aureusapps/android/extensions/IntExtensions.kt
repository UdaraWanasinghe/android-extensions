package com.aureusapps.android.extensions

val Int.opaque get() = (0xFF shl 24) or (0x00FFFFFF and this)