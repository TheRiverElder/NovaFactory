package io.theriverelder.novafactory.util.io.json

interface GenericType<K, T> {
    val key: K
    fun create(): T
    fun restore(json: JsonObject): T
}