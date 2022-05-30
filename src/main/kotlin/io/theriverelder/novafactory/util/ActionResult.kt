package io.theriverelder.novafactory.util

import java.util.*

data class ActionResult<M, E>(
    val succeeded: Boolean,
    val message: M,
    val extra: E,
)

fun <TResult> wrap(fn: () -> TResult): ActionResult<String, TResult?> {
    return try {
        ActionResult(true, "", fn())
    } catch (e: Exception) {
        ActionResult(false, Objects.toString(e.message), null)
    }
}