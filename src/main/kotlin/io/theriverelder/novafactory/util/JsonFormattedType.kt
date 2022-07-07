package io.theriverelder.novafactory.util

import io.theriverelder.novafactory.util.io.json.GenericType
import io.theriverelder.novafactory.util.io.json.JsonObject
import io.theriverelder.novafactory.util.io.json.Persistent

class JsonFormattedType<K, T : Persistent>(
    override val key: K,
    val creator: (type: GenericType<K, T>) -> T,
) : GenericType<K, T> {

    override fun create() = creator(this)

    override fun restore(json: JsonObject): T {
        val result = creator(this)
        result.read(json)
        return result
    }
}