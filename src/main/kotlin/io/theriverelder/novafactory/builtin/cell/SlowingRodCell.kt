package io.theriverelder.novafactory.builtin.cell

import io.theriverelder.novafactory.data.cell.Cell
import io.theriverelder.novafactory.data.cell.ValuePack
import io.theriverelder.novafactory.util.io.json.*

class SlowingRodCell(type: GenericType<String, Cell>, init: SlowingRodCell.() -> Unit = {}) : Cell(type) {

    override var mass: Double = 1000.0
    override var heat: Double = 0.0
    override var heatTransferFactor: Double = 0.0
    override var heatCapacity: Double = 1.0
    var slowRate: Double = 0.0

    init {
        init()
    }

    override fun read(json: JsonObject) {
        super.read(json)
        mass = json["mass"].number.toDouble()
        heatTransferFactor = json["heatTransferFactor"].number.toDouble()
        heatCapacity = json["heatCapacity"].number.toDouble()
        slowRate = json["slowRate"].number.toDouble()
    }

    override fun write(): JsonObject {
        return super.write().concat(
            "mass" to JsonNumber(mass),
            "slowRate" to JsonNumber(slowRate),
        )
    }

    override fun receive(valuePack: ValuePack) {
        when (valuePack.valueType) {
            "radiation" -> valuePack.consume(slowRate * valuePack.amount)
            "heat" -> {
                val p = slowRate * valuePack.amount
                heat += p
                valuePack.consume(p)
            }
            else -> super.receive(valuePack)
        }
    }
}