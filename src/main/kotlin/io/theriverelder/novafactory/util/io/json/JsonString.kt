package io.theriverelder.novafactory.util.io.json

import io.theriverelder.novafactory.util.escape
import java.lang.StringBuilder

class JsonString(val value: String): JsonSerializable {

    override fun serialize(output: StringBuilder) {
        output.append('"')
        output.append(value.escape())
        output.append('"')
    }
}