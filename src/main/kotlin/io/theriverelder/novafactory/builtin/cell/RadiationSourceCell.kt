package io.theriverelder.novafactory.builtin.cell

import io.theriverelder.novafactory.data.cell.Cell
import io.theriverelder.novafactory.data.cell.ValuePack
import io.theriverelder.novafactory.util.io.json.JsonNumber
import io.theriverelder.novafactory.util.io.json.JsonObject
import io.theriverelder.novafactory.util.io.json.number
import io.theriverelder.novafactory.util.io.json.unaryPlus
import io.theriverelder.novafactory.util.time.DeltaTime

class RadiationSourceCell(init: RadiationSourceCell.() -> Unit = {}) : Cell() {

    override var mass: Double = 1.0
    override var heat: Double = 0.0
    override var heatTransferFactor: Double = 0.0
    override var heatCapacity: Double = 1.0
    var radiationSpeed: Double = 0.0

    override fun read(json: JsonObject) {
        mass = json["mass"].number.toDouble()
        heat = json["heat"].number.toDouble()
        heatTransferFactor = json["heatTransferFactor"].number.toDouble()
        heatCapacity = json["heatCapacity"].number.toDouble()
        radiationSpeed = json["radiationSpeed"].number.toDouble()
    }

    override fun write(): JsonObject {
        return JsonObject(
            "id" to +javaClass.simpleName,
            "mass" to JsonNumber(mass),
            "heat" to JsonNumber(heat),
            "heatTransferFactor" to JsonNumber(heatTransferFactor),
            "heatCapacity" to JsonNumber(heatCapacity),
            "radiationSpeed" to JsonNumber(radiationSpeed),
        )
    }

    init {
        init()
    }

    override fun onRequest(valuePack: ValuePack): ValuePack {
        TODO("Not yet implemented")
    }

    override fun onTick() {
        val slot = slot!!
        slot.reactor.spread(slot, "radiation", radiationSpeed)
    }
}