package io.theriverelder.novafactory.util.io.json

import java.lang.StringBuilder

interface JsonSerializable {

    fun serialize(output: StringBuilder)

}

val JsonSerializable.number: Number
    get() = (this as? JsonNumber)?.value ?: throw Exception("Is not number")

val JsonSerializable.string: String
    get() = (this as? JsonString)?.value ?: throw Exception("Is not string")

val JsonSerializable.boolean: Boolean
    get() = (this as? JsonBoolean)?.value ?: throw Exception("Is not boolean")

val JsonSerializable.obj: JsonObject
    get() = (this as? JsonObject) ?: throw Exception("Is not object")

val JsonSerializable.array: JsonArray
    get() = (this as? JsonArray) ?: throw Exception("Is not array")