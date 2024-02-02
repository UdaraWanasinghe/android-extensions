package com.aureusapps.android.extensions

import android.content.res.Resources
import android.content.res.TypedArray
import android.util.TypedValue
import androidx.annotation.ArrayRes
import androidx.annotation.StyleableRes
import androidx.core.content.res.getFloatOrThrow

/**
 * Retrieves an enum attribute from the TypedArray.
 *
 * @param index The index of the enum attribute to retrieve from the TypedArray.
 * @param default The default value to return if the attribute is not found.
 *
 * @return The enum attribute if found, or the default value if not found.
 */
inline fun <reified T : Enum<T>> TypedArray.getEnum(index: Int, default: T) = getInt(index, -1).let {
    if (it >= 0) enumValues<T>()[it] else default
}

/**
 * Retrieves an enum attribute from the TypedArray or throws an exception if not found.
 *
 * @param index The index of the enum attribute to retrieve from the TypedArray.
 * @throws IllegalArgumentException if the attribute is not found or cannot be parsed into the enum type T.
 *
 * @return The enum attribute if found.
 */
inline fun <reified T : Enum<T>> TypedArray.getEnumOrThrow(index: Int) = getInt(index, -1).let {
    if (it >= 0) enumValues<T>()[it] else throw IllegalArgumentException("Attribute not defined in set")
}

/**
 * Retrieves a fraction attribute from the TypedArray.
 *
 * @param index The index of the fraction attribute to retrieve from the TypedArray.
 * @param default The default value to return if the attribute is not found.
 *
 * @return The fraction attribute if found, or the default value if not found.
 */
fun TypedArray.getFraction(index: Int, default: Float): Float {
    return getFraction(index, 1, 1, default)
}

/**
 * Retrieves a fraction attribute from the TypedArray or throws [IllegalArgumentException] if not found.
 *
 * @param index The index of the fraction attribute to retrieve from the TypedArray.
 * @throws IllegalArgumentException if the attribute is not found.
 *
 * @return The fraction attribute if found.
 */
fun TypedArray.getFractionOrThrow(index: Int): Float {
    checkAttribute(index)
    return getFraction(index, 1, 1, 0f)
}

/**
 * Retrieves a float or fraction attribute from the TypedArray.
 *
 * @param index The index of the float or fraction attribute to retrieve from the TypedArray.
 * @param default The default value to return if the attribute is not found.
 *
 * @return The float or fraction attribute if found, or the default value if not found.
 */
fun TypedArray.getFloatOrFraction(index: Int, default: Float): Float {
    return when (getType(index)) {
        TypedValue.TYPE_FRACTION -> getFraction(index, 1, 1, default)
        TypedValue.TYPE_FLOAT -> getFloat(index, default)
        else -> default
    }
}

/**
 * Retrieves a float or fraction attribute from the TypedArray or throws an exception if not found.
 *
 * @param index The index of the float or fraction attribute to retrieve from the TypedArray.
 * @throws IllegalArgumentException if the attribute is not found or it's not float or a fraction.
 *
 * @return The float or fraction attribute if found.
 */
fun TypedArray.getFloatOrFractionOrThrow(index: Int): Float {
    return when (getType(index)) {
        TypedValue.TYPE_FLOAT -> getFloatOrThrow(index)
        TypedValue.TYPE_FRACTION -> getFractionOrThrow(index)
        else -> throw IllegalArgumentException("Given resource index is not a float or a fraction")
    }
}

/**
 * Retrieves an integer array attribute from the TypedArray.
 *
 * @param resources The Resources object used to access resources.
 * @param index The index of the integer array attribute to retrieve from the TypedArray.
 * @param default The resource ID of the default integer array to return if the attribute is not found
 *               or if it's not an integer array.
 *
 * @return The integer array attribute if found, or the default integer array specified by the resource ID.
 */
fun TypedArray.getIntArray(
    resources: Resources,
    @StyleableRes index: Int,
    @ArrayRes default: Int
): IntArray {
    return resources.getIntArray(getResourceId(index, default))
}

/**
 * Retrieves an integer array attribute from the TypedArray or throws an exception if not found.
 *
 * @param resources The Resources object used to access resources.
 * @param index The index of the integer array attribute to retrieve from the TypedArray.
 * @throws Resources.NotFoundException if the attribute is not found.
 *
 * @return The integer array attribute if found.
 */
fun TypedArray.getIntArrayOrThrow(
    resources: Resources,
    @StyleableRes index: Int,
): IntArray {
    return resources.getIntArray(getResourceId(index, 0))
}

private fun TypedArray.checkAttribute(@StyleableRes index: Int) {
    if (!hasValue(index)) {
        throw IllegalArgumentException("Attribute not defined in set.")
    }
}