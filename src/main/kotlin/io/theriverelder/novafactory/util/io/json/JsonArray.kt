package io.theriverelder.novafactory.util.io.json

import java.lang.StringBuilder

class JsonArray: JsonSerializable, Iterable<JsonSerializable> {

    val value: Array<out JsonSerializable>

    constructor(vararg elements: JsonSerializable) {
        value = elements
    }

    constructor(elements: List<JsonSerializable>) {
        value = elements.toTypedArray()
    }

    override fun serialize(output: StringBuilder) {
        output.append("[")
        val itr = value.toList().iterator()
        while (itr.hasNext()) {
            itr.next().serialize(output)
            if (itr.hasNext()) {
                output.append(",")
            }
        }
        output.append("]")
    }

    override fun iterator(): Iterator<JsonSerializable> = value.iterator()
}