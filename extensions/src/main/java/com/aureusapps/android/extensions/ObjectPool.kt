package com.aureusapps.android.extensions

/**
 * A generic object pool implementation in Kotlin.
 *
 * @property poolSize The maximum size of the object pool. Defaults to 10 if not specified.
 * @property objectFactory A lambda function responsible for creating new instances of type T.
 */
class ObjectPool<T>(
    private val poolSize: Int = 10,
    private val objectFactory: () -> T,
) {

    private val objects = mutableListOf<T>()

    init {
        refillPool()
    }

    private fun refillPool() {
        repeat(poolSize) {
            objects.add(objectFactory())
        }
    }

    fun acquire(): T {
        if (objects.isEmpty()) {
            refillPool()
        }
        return objects.removeAt(objects.size - 1)
    }

    fun release(obj: T) {
        if (objects.size < poolSize) {
            objects.add(obj)
        }
    }

}