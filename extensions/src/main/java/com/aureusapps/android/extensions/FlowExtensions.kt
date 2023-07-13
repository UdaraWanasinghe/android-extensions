package com.aureusapps.android.extensions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Folds the given flow with operation, emitting every intermediate result that is not null, including initial value.
 *
 * @param initial Initial value to emit.
 * @param operation Accumulation operation.
 *
 * @return Returns new flow with the applied operation to accumulate non-null values.
 */
fun <T, R> Flow<T>.scanNotNull(
    initial: R,
    operation: suspend (accumulator: R, value: T) -> R?
): Flow<R> = flow {
    var accumulator: R = initial
    emit(initial)
    collect { value ->
        accumulator = operation(accumulator, value) ?: return@collect
        emit(accumulator)
    }
}