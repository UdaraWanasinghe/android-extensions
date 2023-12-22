package com.aureusapps.android.extensions

import org.json.JSONArray

/**
 * Extension function for JSONArray to map its elements to a list of elements of type T.
 *
 * @param block The transformation function that takes an element from JSONArray and returns an element of type T.
 * @return A list containing elements of type T resulting from the transformation of JSONArray's elements.
 */
inline fun <T> JSONArray.map(block: (element: Any) -> T): List<T> =
    // Map through each index in the JSONArray and apply the transformation function to its elements
    (0 until length()).map { block(get(it)) }