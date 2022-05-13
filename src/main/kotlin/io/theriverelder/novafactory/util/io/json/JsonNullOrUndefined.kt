package io.theriverelder.novafactory.util.io.json

import java.lang.StringBuilder

class JsonNullOrUndefined(val isNull: Boolean): JsonSerializable {
    override fun serialize(output: StringBuilder) {
        output.append(if (isNull) "null" else "undefined")
    }

}

val JSON_NULL = JsonNullOrUndefined(true)
val JSON_UNDEFINED = JsonNullOrUndefined(false)

fun jsonNullOrUndefined(value: Boolean) = if (value) JSON_NULL else JSON_UNDEFINED