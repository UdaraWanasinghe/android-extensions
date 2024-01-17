package com.aureusapps.android.extensions

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat

private val dimenMap = mapOf(
    "px" to TypedValue.COMPLEX_UNIT_PX,
    "dip" to TypedValue.COMPLEX_UNIT_DIP,
    "dp" to TypedValue.COMPLEX_UNIT_DIP,
    "sp" to TypedValue.COMPLEX_UNIT_SP,
    "pt" to TypedValue.COMPLEX_UNIT_PT,
    "in" to TypedValue.COMPLEX_UNIT_IN,
    "mm" to TypedValue.COMPLEX_UNIT_MM
)

/**
 * Retrieves the color attribute value specified by the attribute name from the AttributeSet.
 *
 * @param context The context used to obtain resources and display metrics.
 * @param attrName The name of the color attribute to retrieve from the AttributeSet.
 * @param defaultColor The default color to be returned if the attribute is not present or
 *        cannot be resolved. Default is Color.BLACK.
 *
 * @return The color integer if the attribute is present, or the default color if not found.
 */
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

/**
 * Retrieves the ColorStateList attribute specified by the attribute name from the AttributeSet.
 *
 * @param context The context used to obtain resources and display metrics.
 * @param attrName The name of the ColorStateList attribute to retrieve from the AttributeSet.
 *
 * @return The ColorStateList if the attribute is present, or null if not found.
 */
fun AttributeSet.getColorStateListAttribute(
    context: Context,
    attrName: String,
): ColorStateList? {
    val attrIndex = getAttributePosition(attrName)
    return if (attrIndex < 0) {
        null
    } else {
        val attrValue = getAttributeValue(attrIndex)
        return when {
            attrValue.startsWith("@") -> {
                val colorResId = attrValue.replace("@", "").toInt()
                getColorStateList(context, colorResId)
            }

            attrValue.startsWith("?") -> {
                val colorAttrId = attrValue.replace("?", "").toInt()
                val outValue = TypedValue()
                if (context.theme.resolveAttribute(colorAttrId, outValue, true)) {
                    getColorStateList(context, outValue.resourceId)
                } else {
                    null
                }
            }

            else -> ColorStateList.valueOf(
                getAttributeUnsignedIntValue(attrIndex, Color.TRANSPARENT)
            )
        }
    }
}

private fun getColorStateList(context: Context, @ColorRes colorResId: Int): ColorStateList? {
    return ResourcesCompat.getColorStateList(context.resources, colorResId, context.theme)
}

/**
 * Retrieves the float attribute value specified by the attribute name from the AttributeSet.
 *
 * @param context The context used to obtain resources and display metrics.
 * @param attrName The name of the float attribute to retrieve from the AttributeSet.
 *
 * @return The float value if the attribute is present, or null if not found.
 */
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

/**
 * Retrieves the dimension attribute value specified by the attribute name from the AttributeSet.
 *
 * @param context The context used to obtain the display metrics for unit conversion.
 * @param attrName The name of the dimension attribute to retrieve from the AttributeSet.
 *
 * @return The dimension value in pixels if the attribute is present, or null if not found.
 */
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

/**
 * Retrieves the position of the specified attribute in the AttributeSet.
 *
 * This function iterates through the attributes in the AttributeSet and returns
 * the position (index) of the attribute with the given name. If the attribute is
 * not found, it returns -1.
 *
 * @param attrName The name of the attribute whose position is to be retrieved.
 * @return The position (index) of the attribute in the AttributeSet, or -1 if not found.
 * @throws IllegalArgumentException if the provided attribute name is empty.
 */
fun AttributeSet.getAttributePosition(attrName: String): Int {
    require(attrName.isNotBlank()) { "Attribute name must not be empty." }
    return (0 until attributeCount).firstOrNull { i -> getAttributeName(i) == attrName } ?: -1
}