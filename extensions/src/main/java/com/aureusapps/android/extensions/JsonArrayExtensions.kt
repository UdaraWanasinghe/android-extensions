package com.aureusapps.android.extensions

import org.json.JSONArray
import org.json.JSONObject

fun <T> JSONArray.mapJSONObject(block: (JSONObject) -> T): List<T> {
    return List(length()) {
        block(getJSONObject(it))
    }
}

fun <T> JSONArray.mapJSONArray(block: (JSONArray) -> T): List<T> {
    return List(length()) {
        block(getJSONArray(it))
    }
}

fun <T> JSONArray.mapString(block: (String) -> T): List<T> {
    return List(length()) {
        block(getString(it))
    }
}