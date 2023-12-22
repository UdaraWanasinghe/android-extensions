package com.aureusapps.android.extensions

object JSON {

    fun toBoolean(value: Any): Boolean? {
        return when (value) {
            is Boolean -> value
            is String -> when {
                "true".equals(value, ignoreCase = true) -> true
                "false".equals(value, ignoreCase = true) -> false
                else -> null
            }

            else -> null
        }
    }

    fun toDouble(value: Any): Double? {
        return when (value) {
            is Double -> value
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull()
            else -> null
        }
    }

    fun toInteger(value: Any): Int? {
        return when (value) {
            is Int -> value
            is Number -> value.toInt()
            is String -> value.toIntOrNull()
            else -> null
        }
    }

    fun toLong(value: Any): Long? {
        return when (value) {
            is Long -> value
            is Number -> value.toLong()
            is String -> value.toLongOrNull()
            else -> null
        }
    }

    fun toString(value: Any): String {
        return when (value) {
            is String -> value
            else -> value.toString()
        }
    }

}