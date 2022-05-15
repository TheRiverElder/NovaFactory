package io.theriverelder.novafactory.data.reactor

import io.theriverelder.novafactory.data.cell.Cell
import io.theriverelder.novafactory.data.cell.ValuePack
import io.theriverelder.novafactory.util.io.json.JsonNumber
import io.theriverelder.novafactory.util.io.json.JsonObject
import io.theriverelder.novafactory.util.io.json.number

class WallCell(init: WallCell.() -> Unit = {}) : Cell() {

    init {
        init()
    }

    override var mass: Double = 1.0
    override var heat: Double = 0.0
    override var heatTransferFactor: Double = 0.0
    override var heatCapacity: Double = 0.0

    override fun onRequest(valuePack: ValuePack): ValuePack {
        TODO("Not yet implemented")
    }

    override fun onTick() {
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