package com.aureusapps.android.extensions

import android.os.Build
import android.widget.TextView
import androidx.annotation.StyleRes

@Suppress("DEPRECATION")
fun TextView.setTextStyle(@StyleRes resId: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        setTextAppearance(resId)
    } else {
        setTextAppearance(context, resId)
    }
}

/**
 * Updates the text of the TextView and sets a tag to indicate whether the text was edited by the user or not.
 *
 * @param text The new text to set in the TextView.
 */
private fun TextView.updateText(text: String) {
    setTag(R.id.text_view_user_edit_key, false)
    setText(text)
    setTag(R.id.text_view_user_edit_key, true)
}

/**
 * Determines whether the text of the TextView has been edited by the user.
 *
 * @return True if the text has been edited by the user, false otherwise.
 */
private val TextView.isUserEdit: Boolean
    get() {
        val tag = getTag(R.id.text_view_user_edit_key)
        return tag == null || tag == true
    }