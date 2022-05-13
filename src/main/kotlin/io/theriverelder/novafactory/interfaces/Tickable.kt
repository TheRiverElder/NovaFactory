package io.theriverelder.novafactory.interfaces

interface Tickable {
    fun tick() = onTick()
    fun onTick()
}