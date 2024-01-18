package com.aureusapps.android.extensions

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