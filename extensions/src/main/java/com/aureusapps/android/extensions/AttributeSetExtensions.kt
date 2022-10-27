package com.aureusapps.android.extensions

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import androidx.core.content.ContextCompat

private val dimenMap = mapOf(
    "px" to TypedValue.COMPLEX_UNIT_PX,
    "dip" to TypedValue.COMPLEX_UNIT_DIP,
    "dp" to TypedValue.COMPLEX_UNIT_DIP,
    "sp" to TypedValue.COMPLEX_UNIT_SP,
    "pt" to TypedValue.COMPLEX_UNIT_PT,
    "in" to TypedValue.COMPLEX_UNIT_IN,
    "mm" to TypedValue.COMPLEX_UNIT_MM
)

fun AttributeSet.getColorAttribute(
    context: Context,
    attrName: String,
    defaultColor: Int = Color.BLACK
): Int? {
    return try {
        val attrIndex = getAttributePosition(attrName)
        if (attrIndex < 0) {
            null
        } else {
            val attrValue = getAttributeValue(attrIndex)
            return when {
                attrValue.contains("@") -> {
                    val colorResId = attrValue.replace("@", "").toInt()
                    ContextCompat.getColor(context, colorResId)
                }
                attrValue.contains("?") -> {
                    val colorAttrId = attrValue.replace("?", "").toInt()
                    val outValue = TypedValue()
                    if (context.theme.resolveAttribute(colorAttrId, outValue, true)) {
                        outValue.data
                    } else {
                        defaultColor
                    }
                }
                else -> {
                    getAttributeUnsignedIntValue(attrIndex, defaultColor)
                }
            }
        }
    } catch (e: Exception) {
        return defaultColor
    }
}

fun AttributeSet.getFloatAttribute(context: Context, attrName: String): Float? {
    val attrIndex = getAttributePosition(attrName)
    return if (attrIndex < 0) {
        null
    } else {
        val attrValue = getAttributeValue(attrIndex)
        return when {
            attrValue.startsWith("@") -> {
                val floatResId = attrValue.replace("@", "").toInt()
                context.resources.getDimension(floatResId)
            }
            attrValue.startsWith("?") -> {
                val floatAttrId = attrValue.replace("?", "").toInt()
                val outValue = TypedValue()
                if (context.theme.resolveAttribute(floatAttrId, outValue, true)) {
                    outValue.getDimension(context.resources.displayMetrics)
                } else {
                    null
                }
            }
            else -> {
                getAttributeFloatValue(attrIndex, 0f)
            }
        }
    }
}

fun AttributeSet.getDimensionAttribute(context: Context, attrName: String): Float? {
    val attrIndex = getAttributePosition(attrName)
    if (attrIndex < 0) {
        return null
    }
    val attrValue = getAttributeValue(attrIndex)
    return when {
        attrValue.startsWith("@") -> {
            val dimenResId = attrValue.replace("@", "").toInt()
            context.resources.getDimension(dimenResId)
        }
        attrValue.startsWith("?") -> {
            val dimenAttrId = attrValue.replace("?", "").toInt()
            val outValue = TypedValue()
            if (context.theme.resolveAttribute(dimenAttrId, outValue, true)) {
                outValue.getDimension(context.resources.displayMetrics)
            } else {
                null
            }
        }
        else -> {
            val dimenUnit = dimenMap.entries.firstOrNull { attrValue.endsWith(it.key) }
            if (dimenUnit == null) {
                return attrValue.toFloat()
            } else {
                return TypedValue.applyDimension(
                    dimenUnit.value,
                    attrValue.replace(dimenUnit.key, "").toFloat(),
                    context.resources.displayMetrics
                )
            }
        }
    }
}

fun AttributeSet.getAttributePosition(attrName: String): Int {
    return (0 until attributeCount)
        .firstOrNull { i -> getAttributeName(i) == attrName } ?: -1
}