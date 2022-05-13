package io.theriverelder.novafactory.util.io.json

import java.lang.StringBuilder
import kotlin.math.pow

class JsonNumber(val value: Number) : JsonSerializable {

    override fun serialize(output: StringBuilder) {
        output.append(value)
    }

}