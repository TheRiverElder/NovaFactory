package io.theriverelder.novafactory.util.math

import io.theriverelder.novafactory.util.io.json.JsonNumber
import io.theriverelder.novafactory.util.io.json.JsonObject
import io.theriverelder.novafactory.util.io.json.Persistent
import io.theriverelder.novafactory.util.io.json.number

class UidGen(initialValue: Int = 0) : Persistent {
    private var counter: Int = initialValue

    fun gen() = counter++

    override fun read(json: JsonObject) {
        counter = json["counter"].number.toInt()
    }

    override fun write(): JsonObject = JsonObject(
        "counter" to JsonNumber(counter),
    )
}