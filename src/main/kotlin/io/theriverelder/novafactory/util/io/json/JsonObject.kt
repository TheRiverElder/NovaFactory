package io.theriverelder.novafactory.util.io.json

import java.lang.StringBuilder

class JsonObject(vararg val value: Pair<String, JsonSerializable>): JsonSerializable {

    val map = mapOf(*value)

    override fun serialize(output: StringBuilder) {
        output.append("{")
        val itr = value.toList().iterator()
        while (itr.hasNext()) {
            val elem = itr.next()
            output.append('"')
            output.append(elem.first)
            output.append('"')
            output.append(":")
            elem.second.serialize(output)
            if (itr.hasNext()) {
                output.append(",")
            }
        }
        output.append("}")
    }

    operator fun get(key: String): JsonSerializable = map[key] ?: throw Exception("不存在键：$key")

    fun concat(vararg newValue: Pair<String, JsonSerializable>): JsonObject {
        return JsonObject(*(map.entries.map { it.toPair() }.toTypedArray() + newValue))
    }
}