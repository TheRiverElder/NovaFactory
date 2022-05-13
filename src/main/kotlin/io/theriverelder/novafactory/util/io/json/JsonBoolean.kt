package io.theriverelder.novafactory.util.io.json

import java.lang.StringBuilder

class JsonBoolean(val value: Boolean): JsonSerializable {
    override fun serialize(output: StringBuilder) {
        output.append(value)
    }
}

val TRUE = JsonBoolean(true)
val FALSE = JsonBoolean(false)

fun jsonBoolean(value: Boolean) = if (value) TRUE else FALSE