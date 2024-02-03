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