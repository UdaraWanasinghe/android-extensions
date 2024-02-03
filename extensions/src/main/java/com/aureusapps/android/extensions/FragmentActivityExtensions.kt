package com.aureusapps.android.extensions

import android.view.View
import androidx.fragment.app.FragmentActivity

/**
 * Finds a view that was identified by the given resource ID in the activity's content view
 * or in any attached fragments' layouts.
 *
 * This method performs a recursive search in the activity's content view and all attached fragments'
 * layouts to find the specified view.
 *
 * @param id the ID of the view to search for.
 *
 * @return The view if found, or `null` otherwise.
 */
fun <T : View> FragmentActivity.findViewRecursivelyById(id: Int): T? {
    return findViewById(id) ?: run {
        val fragments = supportFragmentManager.fragments
        for (fragment in fragments) {
            val view = fragment.findViewById<T>(id)
            if (view != null) return view
        }
        return null
    }
}