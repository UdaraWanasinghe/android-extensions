package com.aureusapps.android.extensions

/**
 * Performs the given [action] on the list if it is not empty.
 *
 * @param action the action to be performed on the non-empty list.
 */
fun <T> List<T>.ifNotEmpty(action: (List<T>) -> Unit) {
    if (!isEmpty()) {
        action(this)
    }
}

/**
 * Appends the elements of the given [elements] list to this list.
 *
 * @param elements the list of elements to be appended.
 * @return the list with the appended elements.
 */
fun <T> List<T>.append(elements: List<T>): List<T> {
    return if (this is MutableList) {
        addAll(elements)
        this
    } else {
        toMutableList().also { it.addAll(elements) }
    }
}