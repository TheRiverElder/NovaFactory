package io.theriverelder.novafactory.builtin.cell

import io.theriverelder.novafactory.data.cell.Cell
import io.theriverelder.novafactory.data.cell.ValuePack
import io.theriverelder.novafactory.util.io.json.*
import io.theriverelder.novafactory.util.math.ease0to1
import io.theriverelder.novafactory.util.math.clamp

class GeneratorCell(type: GenericType<String, Cell>, init: GeneratorCell.() -> Unit = {}) : Cell(type) {

    override var mass: Double = 1000.0
    override var heat: Double = 0.0
    override var heatTransferFactor: Double = 0.0
    override var heatCapacity: Double = 1.0
    var convertRate: Double = 0.0

    override fun read(json: JsonObject) {
        super.read(json)
        mass = json["mass"].number.toDouble()
        heatTransferFactor = json["heatTransferFactor"].number.toDouble()
        heatCapacity = json["heatCapacity"].number.toDouble()
        convertRate = json["convertRate"].number.toDouble()
    }

    override fun write(): JsonObject {
        return super.write().concat(
            "mass" to JsonNumber(mass),
            "convertRate" to JsonNumber(convertRate),
        )
    }

    init {
        init()
    }

    override fun receive(valuePack: ValuePack) {
        val slot = slot ?: return super.receive(valuePack)
        if (valuePack.valueType != "heat") return super.receive(valuePack)
        slot.reactor.electricityCache += valuePack.consumes(valuePack.amount * convertRate)
        super.receive(valuePack)
    }

    override fun tick() {
        val slot = slot!!

        val rate = (slot.temperature.ease0to1(1 / 3000.0) * convertRate).clamp(0.0, 1.0)
        val dh = (rate * heat).coerceAtLeast(0.0)
        heat -= dh
        slot.reactor.electricityCache += dh
    }
}