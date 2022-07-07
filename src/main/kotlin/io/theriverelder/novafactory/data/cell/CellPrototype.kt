package io.theriverelder.novafactory.data.cell

import io.theriverelder.novafactory.Game
import io.theriverelder.novafactory.interfaces.Unique
import io.theriverelder.novafactory.util.io.json.*
import io.theriverelder.novafactory.util.persistence.restoreCell

class CellPrototype (
    var price: Double = 0.0,
    cell: Cell? = null,
    override var uid: Int = -1,
) : ToJson, Persistent, Unique<Int> {

    lateinit var cell: Cell
    val name: String get() = cell.type.key

    init {
        if (cell != null) {
            this.cell = cell
        }
    }

    fun create(): Cell {
        val c = cell.copy()
        c.uid = Game.factory.cellUidGen.gen()
        return c
    }

    override fun toJson(): JsonSerializable {
        return JsonObject(
            "uid" to JsonNumber(uid),
            "name" to +name,
            "price" to JsonNumber(price),
        )
    }

    override fun read(json: JsonObject) {
        uid = json["uid"].number.toInt()
        price = json["price"].number.toDouble()
        cell = restoreCell(json["cell"].obj)
    }

    override fun write(): JsonObject {
        return JsonObject(
            "uid" to JsonNumber(uid),
            "price" to JsonNumber(price),
            "cell" to cell.write(),
        )
    }

    override fun toString(): String {
        return "{$name, c$price}"
    }
}