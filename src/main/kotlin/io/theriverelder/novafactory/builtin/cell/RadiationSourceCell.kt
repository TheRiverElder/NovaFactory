package io.theriverelder.novafactory.builtin.cell

import io.theriverelder.novafactory.data.cell.Cell
import io.theriverelder.novafactory.data.cell.ValuePack
import io.theriverelder.novafactory.util.io.json.*
import io.theriverelder.novafactory.util.time.DeltaTime

class RadiationSourceCell(type: GenericType<String, Cell>, init: RadiationSourceCell.() -> Unit = {}) : Cell(type) {

    override var mass: Double = 1.0
    override var heat: Double = 0.0
    override var heatTransferFactor: Double = 0.0
    override var heatCapacity: Double = 1.0
    var radiationSpeed: Double = 0.0

    override fun read(json: JsonObject) {
        super.read(json)
        mass = json["mass"].number.toDouble()
        heatTransferFactor = json["heatTransferFactor"].number.toDouble()
        heatCapacity = json["heatCapacity"].number.toDouble()
        radiationSpeed = json["radiationSpeed"].number.toDouble()
    }

    override fun write(): JsonObject {
        return super.write().concat(
            "mass" to JsonNumber(mass),
            "radiationSpeed" to JsonNumber(radiationSpeed),
        )
    }

    init {
        init()
    }

    override fun tick() {
        val slot = slot!!
        slot.reactor.spread(slot, "neutron", radiationSpeed)
    }
}