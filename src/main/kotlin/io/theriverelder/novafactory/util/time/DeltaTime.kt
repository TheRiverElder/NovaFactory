package io.theriverelder.novafactory.util.time

import io.theriverelder.novafactory.util.io.json.JsonNumber
import io.theriverelder.novafactory.util.io.json.JsonObject
import io.theriverelder.novafactory.util.io.json.JsonSerializable
import io.theriverelder.novafactory.util.io.json.ToJson

data class DeltaTime(val previousTime: Long, val currentTime: Long) : ToJson {
    val delta: Long = currentTime - previousTime
    val seconds: Double = delta / 1000.0

    override fun toJson(): JsonSerializable {
        return JsonObject(
            "previousTime" to JsonNumber(previousTime),
            "currentTime" to JsonNumber(currentTime),
        )
    }
}