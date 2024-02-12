package com.aureusapps.android.extensions

import android.view.View
import androidx.fragment.app.Fragment

/**
 * Retrieves the Fragment with the specified tag in the fragment tree.
 *
 * @param tag The tag used to identify the Fragment within the FragmentManager.
 *
 * @return The Fragment instance if found, or null if not found.
 */
fun Fragment.findFragmentByTag(tag: String): Fragment? {
    if (this.tag == tag) return this
    val foundFragment = childFragmentManager.findFragmentByTag(tag)
    if (foundFragment != null) {
        return foundFragment
    }
    val parentFragment = parentFragment
    if (parentFragment != null) {
        return parentFragment.findFragmentByTag(tag)
    }
    return null
}

/**
 * Finds the first descendant view with the given ID.
 *
 * @param id the ID to search for.
 *
 * @return The view if found or `null` if not found.
 */
fun <T : View> Fragment.findViewById(id: Int): T? {
    val view = view ?: return null
    return view.findViewById(id)
}

/**
 * Recursively searches for a view with the specified [id] in the Fragment's view hierarchy.
 *
 * @param id The resource ID of the View to find.
 * @return The View with the specified [id], or null if no such View is found.
 */
fun <T : View> Fragment.findViewRecursivelyById(id: Int): T? {
    return findViewById(id) ?: run {
        val fragments = childFragmentManager.fragments
        for (fragment in fragments) {
            val view = fragment.findViewById<T>(id)
            if (view != null) return view
        }
        return null
    }
}