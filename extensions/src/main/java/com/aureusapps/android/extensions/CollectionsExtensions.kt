package com.aureusapps.android.extensions

fun <T> Collection<T>.copyOf(): Collection<T> {
    return mutableListOf<T>().also { it.addAll(this) }
}

fun <T> MutableList<T>.copyOf(): MutableList<T> {
    return mutableListOf<T>().also { it.addAll(this) }
}