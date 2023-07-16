package com.aureusapps.android.extensions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlin.experimental.ExperimentalTypeInference

/**
 * Folds the given flow with [operation], emitting every intermediate non-null result, including [initial] value.
 * Note that initial value should be immutable (or should not be mutated) as it is shared between different collectors.
 *
 * If the result of [operation] is `null`, it will be skipped and not emitted in the resulting flow.
 *
 * For example:
 * ```
 * flowOf(1, 2, 3).scanNotNull(emptyList<Int>()) { acc, value -> if (value == 2) null else acc + value }.toList()
 * ```
 * will produce `[[], [1], [1, 3]]`.
 */
@OptIn(ExperimentalTypeInference::class)
fun <T, R> Flow<T>.scanNotNull(
    initial: R,
    @BuilderInference operation: suspend (accumulator: R, value: T) -> R?
): Flow<R> = flow {
    var accumulator: R = initial
    emit(initial)
    collect { value ->
        accumulator = operation(accumulator, value) ?: return@collect
        emit(accumulator)
    }
}

/**
 * Applies a transformation to each element emitted by the upstream flow and emits the intermediate results
 * as a new [Flow]. The last transformed value emitted to the downstream flow will be accumulated.
 *
 * For example:
 * ```
 * flow.scanTransform(0) { acc, value -> emit(acc); emit(acc + value) }
 * ```
 * will produce `[0, 0, 1, 1, 3, 3, 6]`
 */
@OptIn(ExperimentalTypeInference::class)
fun <T, R> Flow<T>.scanTransform(
    initial: R,
    @BuilderInference operation: suspend FlowCollector<R>.(accumulator: R, value: T) -> Unit
): Flow<R> = flow {
    var accumulator: R = initial
    emit(initial)
    val collector = FlowCollector<R> {
        accumulator = it
        emit(it)
    }
    collect { value ->
        operation(collector, accumulator, value)
    }
}