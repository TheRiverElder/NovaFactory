package io.theriverelder.novafactory.util

data class ActionResult<M, E>(
    val succeeded: Boolean,
    val message: M,
    val extra: E,
)