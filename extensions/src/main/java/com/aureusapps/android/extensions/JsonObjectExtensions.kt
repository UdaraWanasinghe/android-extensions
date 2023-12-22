package com.aureusapps.android.extensions

import org.json.JSONObject

/**
 * Extension function for JSONObject to map its keys and values to a list of elements of type T.
 *
 * @param block The transformation function that takes a key-value pair and returns an element of type T.
 * @return A list containing elements of type T resulting from the transformation of JSONObject's keys and values.
 */
inline fun <T> JSONObject.map(block: (key: String, value: Any) -> T): List<T> {
    val list = mutableListOf<T>()

    // Iterate through each key in the JSONObject
    for (key in keys()) {
        // Apply the transformation function to the key-value pair and add the result to the list
        list.add(block(key, get(key)))
    }

    // Convert the mutable list to an immutable list and return
    return list.toList()
}