package io.theriverelder.novafactory.util.io.json

interface Persistent {
    fun read(json: JsonObject)
    fun write(): JsonObject
}