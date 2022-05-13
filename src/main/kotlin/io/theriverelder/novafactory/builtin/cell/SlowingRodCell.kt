package io.theriverelder.novafactory.builtin.cell

import io.theriverelder.novafactory.data.cell.Cell
import io.theriverelder.novafactory.data.cell.ValuePack
import io.theriverelder.novafactory.util.io.json.JsonNumber
import io.theriverelder.novafactory.util.io.json.JsonObject
import io.theriverelder.novafactory.util.io.json.number
import io.theriverelder.novafactory.util.io.json.unaryPlus

class SlowingRodCell(init: SlowingRodCell.() -> Unit = {}) : Cell() {

    override var mass: Double = 1000.0
    override var heat: Double = 0.0
    override var heatTransferFactor: Double = 0.0
    override var heatCapacity: Double = 1.0
    var slowRate: Double = 0.0

    override fun read(json: JsonObject) {
        mass = json["mass"].number.toDouble()
        heat = json["heat"].number.toDouble()
        heatTransferFactor = json["heatTransferFactor"].number.toDouble()
        heatCapacity = json["heatCapacity"].number.toDouble()
        slowRate = json["slowRate"].number.toDouble()
    }

    override fun write(): JsonObject {
        return JsonObject(
            "id" to +javaClass.simpleName,
            "mass" to JsonNumber(mass),
            "heat" to JsonNumber(heat),
            "heatTransferFactor" to JsonNumber(heatTransferFactor),
            "heatCapacity" to JsonNumber(heatCapacity),
            "slowRate" to JsonNumber(slowRate),
        )
    }


    init {
        init()
    }

    override fun onRequest(valuePack: ValuePack): ValuePack {
        TODO("Not yet implemented")
    }

    override fun onReceive(valuePack: ValuePack) {
        when (valuePack.valueType) {
            "radiation" -> valuePack.consume(slowRate * valuePack.amount)
            "heat" -> {
                val p = slowRate * valuePack.amount
                heat += p
                valuePack.consume(p)
            }
            else -> super.onReceive(valuePack)
        }
    }

    override fun onTick() {
    }
}