package io.theriverelder.novafactory.data.cell

import io.theriverelder.novafactory.util.io.json.*

class CellPrototype (
    var tip: String,
    var price: Double,
    val provider: () -> Cell
) : ToJson, Persistent {
    fun create(): Cell {
        return provider()
    }

    override fun toJson(): JsonSerializable {
        return JsonObject(
            "tip" to +tip,
            "price" to JsonNumber(price),
        )
    }

    override fun read(json: JsonObject) {
        this.tip = json["tip"].string
        this.price = json["price"].number.toDouble()
    }

    override fun write(): JsonObject {
        return JsonObject(
            "tip" to +tip,
            "price" to JsonNumber(price),
        )
    }

    override fun toString(): String {
        return "{$tip, c$price}"
    }
}