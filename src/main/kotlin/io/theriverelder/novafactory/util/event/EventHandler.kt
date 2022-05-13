package io.theriverelder.novafactory.util.event

interface EventHandler<E> {
    fun handle(event: E)
}