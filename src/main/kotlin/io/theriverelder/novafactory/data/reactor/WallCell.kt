package io.theriverelder.novafactory.data.reactor

import io.theriverelder.novafactory.data.cell.Cell
import io.theriverelder.novafactory.data.cell.ValuePack
import io.theriverelder.novafactory.util.JsonFormattedType
import io.theriverelder.novafactory.util.io.json.GenericType
import io.theriverelder.novafactory.util.io.json.JsonNumber
import io.theriverelder.novafactory.util.io.json.JsonObject
import io.theriverelder.novafactory.util.io.json.number

val WALL_TYPE = JsonFormattedType<String, Cell>("wall") { WallCell() }

class WallCell(init: WallCell.() -> Unit = {}) : Cell(WALL_TYPE) {

    init {
        init()
    }

    override var mass: Double = 1.0
    override var heat: Double = 0.0
    override var heatTransferFactor: Double = 0.0
    override var heatCapacity: Double = 0.0

    override fun tick() {
        heat *= 0.6
    }

    override fun read(json: JsonObject) {
        mass = json["mass"].number.toDouble()
        heat = json["heat"].number.toDouble()
        heatTransferFactor = json["heatTransferFactor"].number.toDouble()
        heatCapacity = json["heatCapacity"].number.toDouble()
    }

    override fun write(): JsonObject {
        return JsonObject(
            "mass" to JsonNumber(mass),
            "heat" to JsonNumber(heat),
            "heatTransferFactor" to JsonNumber(heatTransferFactor),
            "heatCapacity" to JsonNumber(heatCapacity),
        )
    }
}