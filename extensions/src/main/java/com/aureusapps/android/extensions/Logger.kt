package com.aureusapps.android.extensions

import android.util.Log

internal object Logger {

    private const val TAG = "Extensions"

    fun d(msg: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, msg)
        }
    }

    fun e(msg: String) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, msg)
        }
    }

}