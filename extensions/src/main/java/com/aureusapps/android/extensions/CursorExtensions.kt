package com.aureusapps.android.extensions

import android.database.Cursor
import androidx.core.database.getStringOrNull

fun Cursor.getStringList(columnIndex: Int): List<String> {
    return getString(columnIndex).split(",")
}

fun Cursor.getStringListOrNull(columnIndex: Int): List<String>? {
    return getStringOrNull(columnIndex)?.split(",")
}

fun Cursor.getStringListOrEmpty(columnIndex: Int): List<String> {
    return getStringOrNull(columnIndex)?.split(",") ?: listOf()
}

fun Cursor.getIntList(columnIndex: Int): List<Int> {
    return getString(columnIndex).split(",").map { it.toInt() }
}

fun Cursor.getIntListOrNull(columnIndex: Int): List<Int>? {
    return getStringOrNull(columnIndex)?.split(",")?.map { it.toInt() }
}

fun Cursor.getIntListOrEmpty(columnIndex: Int): List<Int> {
    return getStringOrNull(columnIndex)?.split(",")?.map { it.toInt() } ?: listOf()
}

fun Cursor.getLongList(columnIndex: Int): List<Long> {
    return getString(columnIndex).split(",").map { it.toLong() }
}

fun Cursor.getLongListOrNull(columnIndex: Int): List<Long>? {
    return getStringOrNull(columnIndex)?.split(",")?.map { it.toLong() }
}

fun Cursor.getLongListOrEmpty(columnIndex: Int): List<Long> {
    return getStringOrNull(columnIndex)?.split(",")?.map { it.toLong() } ?: listOf()
}

fun Cursor.getFloatList(columnIndex: Int): List<Float> {
    return getString(columnIndex).split(",").map { it.toFloat() }
}

fun Cursor.getFloatListOrNull(columnIndex: Int): List<Float>? {
    return getStringOrNull(columnIndex)?.split(",")?.map { it.toFloat() }
}

fun Cursor.getFloatListOrEmpty(columnIndex: Int): List<Float> {
    return getStringOrNull(columnIndex)?.split(",")?.map { it.toFloat() } ?: listOf()
}

fun Cursor.getDoubleList(columnIndex: Int): List<Double> {
    return getString(columnIndex).split(",").map { it.toDouble() }
}

fun Cursor.getDoubleListOrNull(columnIndex: Int): List<Double>? {
    return getStringOrNull(columnIndex)?.split(",")?.map { it.toDouble() }
}

fun Cursor.getDoubleListOrEmpty(columnIndex: Int): List<Double> {
    return getStringOrNull(columnIndex)?.split(",")?.map { it.toDouble() } ?: listOf()
}

fun Cursor.getBooleanList(columnIndex: Int): List<Boolean> {
    return getString(columnIndex).split(",").map { it.toBoolean() }
}

fun Cursor.getBooleanListOrNull(columnIndex: Int): List<Boolean>? {
    return getStringOrNull(columnIndex)?.split(",")?.map { it.toBoolean() }
}

fun Cursor.getBooleanListOrEmpty(columnIndex: Int): List<Boolean> {
    return getStringOrNull(columnIndex)?.split(",")?.map { it.toBoolean() } ?: listOf()
}

fun Cursor.columnToStringList(columnIndex: Int): List<String> {
    moveToFirst()
    val list = mutableListOf<String>()
    while (!isAfterLast) {
        list.add(getString(columnIndex))
        moveToNext()
    }
    return list
}

fun Cursor.columnToIntList(columnIndex: Int): List<Int> {
    moveToFirst()
    val list = mutableListOf<Int>()
    while (!isAfterLast) {
        list.add(getInt(columnIndex))
        moveToNext()
    }
    return list
}

fun Cursor.columnToLongList(columnIndex: Int): List<Long> {
    moveToFirst()
    val list = mutableListOf<Long>()
    while (!isAfterLast) {
        list.add(getLong(columnIndex))
        moveToNext()
    }
    return list
}

fun Cursor.columnToFloatList(columnIndex: Int): List<Float> {
    moveToFirst()
    val list = mutableListOf<Float>()
    while (!isAfterLast) {
        list.add(getFloat(columnIndex))
        moveToNext()
    }
    return list
}

fun Cursor.columnToDoubleList(columnIndex: Int): List<Double> {
    moveToFirst()
    val list = mutableListOf<Double>()
    while (!isAfterLast) {
        list.add(getDouble(columnIndex))
        moveToNext()
    }
    return list
}