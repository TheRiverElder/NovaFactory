package io.theriverelder.novafactory.builtin.cell

import io.theriverelder.novafactory.data.cell.Cell
import io.theriverelder.novafactory.data.cell.ValuePack
import io.theriverelder.novafactory.util.io.json.JsonNumber
import io.theriverelder.novafactory.util.io.json.JsonObject
import io.theriverelder.novafactory.util.io.json.number
import io.theriverelder.novafactory.util.io.json.unaryPlus
import io.theriverelder.novafactory.util.time.DeltaTime

class CoolingBlockCell(init: CoolingBlockCell.() -> Unit = {}) : Cell() {

    override var mass: Double = 1000.0
    override var heat: Double = 0.0
    override var heatTransferFactor: Double = 0.0
    override var heatCapacity: Double = 1.0
    var consumeRate: Double = 0.0

    override fun read(json: JsonObject) {
        mass = json["mass"].number.toDouble()
        heat = json["heat"].number.toDouble()
        heatTransferFactor = json["heatTransferFactor"].number.toDouble()
        heatCapacity = json["heatCapacity"].number.toDouble()
        consumeRate = json["consumeRate"].number.toDouble()
    }

    override fun write(): JsonObject {
        return JsonObject(
            "id" to +javaClass.simpleName,
            "mass" to JsonNumber(mass),
            "heat" to JsonNumber(heat),
            "heatTransferFactor" to JsonNumber(heatTransferFactor),
            "heatCapacity" to JsonNumber(heatCapacity),
            "consumeRate" to JsonNumber(consumeRate),
        )
    }


    init {
        init()
    }

    override fun onRequest(valuePack: ValuePack): ValuePack {
        TODO("Not yet implemented")
    }

    override fun onTick() {
    }
}