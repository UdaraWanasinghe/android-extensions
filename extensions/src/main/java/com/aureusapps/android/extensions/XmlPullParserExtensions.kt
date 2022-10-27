package com.aureusapps.android.extensions

import org.xmlpull.v1.XmlPullParser

fun XmlPullParser.forEachTag(action: (XmlPullParser) -> Unit) {
    var event = eventType
    while (event != XmlPullParser.END_DOCUMENT) {
        if (event == XmlPullParser.START_TAG) {
            action(this)
        }
        event = next()
    }
}