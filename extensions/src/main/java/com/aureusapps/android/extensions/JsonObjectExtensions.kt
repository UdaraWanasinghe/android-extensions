package com.aureusapps.android.extensions

import org.json.JSONArray
import org.json.JSONObject

fun <T> JSONObject.mapString(block: (String, String) -> T): List<T> {
    val list = ArrayList<T>()
    for (key in keys()) {
        list.add(block(key, getString(key)))
    }
    return list
}

fun <T> JSONObject.mapJSONObject(block: (String, JSONObject) -> T): List<T> {
    val list = ArrayList<T>()
    for (key in keys()) {
        list.add(block(key, getJSONObject(key)))
    }
    return list
}

fun <T> JSONObject.mapJSONArray(block: (String, JSONArray) -> T): List<T> {
    val list = ArrayList<T>()
    for (key in keys()) {
        list.add(block(key, getJSONArray(key)))
    }
    return list
}