package io.theriverelder.novafactory.builtin.cell

import io.theriverelder.novafactory.data.cell.Cell
import io.theriverelder.novafactory.data.cell.ValuePack
import io.theriverelder.novafactory.util.io.json.*
import io.theriverelder.novafactory.util.math.ease0to1
import io.theriverelder.novafactory.util.math.halfLifeLossDuring
import io.theriverelder.novafactory.util.math.limit
import io.theriverelder.novafactory.util.math.shorten

class NuclearRodCell(init: NuclearRodCell.() -> Unit = {}) : Cell() {

    public var fissionRate: Double = 1.0
    public var nuclear: Double = 0.0
    public var nonNuclear: Double = 0.0
    public var radiation: Double = 0.0
    public var depth: Double = 1.0

    override val mass: Double
        get() = nuclear + nonNuclear
    override var heat: Double = 0.0
    override var heatTransferFactor: Double = 0.0
    override var heatCapacity: Double = 1.0

    override fun toJson(): JsonSerializable {
        return (super.toJson() as JsonObject).concat(
            "fissionRate" to JsonNumber(fissionRate),
            "nuclear" to JsonNumber(nuclear),
            "nonNuclear" to JsonNumber(nonNuclear),
            "radiation" to JsonNumber(radiation),
            "depth" to JsonNumber(depth),
        )
    }

    override fun read(json: JsonObject) {
        fissionRate = json["fissionRate"].number.toDouble()
        nuclear = json["nuclear"].number.toDouble()
        nonNuclear = json["nonNuclear"].number.toDouble()
        radiation = json["radiation"].number.toDouble()
        heat = json["heat"].number.toDouble()
        heatTransferFactor = json["heatTransferFactor"].number.toDouble()
        heatCapacity = json["heatCapacity"].number.toDouble()
        depth = json["depth"].number.toDouble()
    }

    override fun write(): JsonObject {
        return JsonObject(
            "id" to +javaClass.simpleName,
            "halfLife" to JsonNumber(fissionRate),
            "nuclear" to JsonNumber(nuclear),
            "nonNuclear" to JsonNumber(nonNuclear),
            "radiation" to JsonNumber(radiation),
            "heat" to JsonNumber(heat),
            "heatTransferFactor" to JsonNumber(heatTransferFactor),
            "heatCapacity" to JsonNumber(heatCapacity),
            "depth" to JsonNumber(depth),
        )
    }

    init {
        init()
    }

    override fun onReceive(valuePack: ValuePack) {
        when (valuePack.valueType) {
            "radiation" -> radiation += valuePack.amount
            else -> super.onReceive(valuePack)
        }
    }

    override fun onAccept(valuePack: ValuePack) {
        when (valuePack.valueType) {
            "radiation" -> radiation += valuePack.amount
            else -> super.onAccept(valuePack)
        }
    }

    override fun onRequest(valuePack: ValuePack): ValuePack {
        TODO("Not yet implemented")
    }

    override fun onTick() {
        val slot = slot!!
        val bonusRate = (1.0 - fissionRate) * 1.0e-2 * radiation.ease0to1(1 / 1e15) * depth
//        println("bonusRate = $bonusRate")
        val rate = (fissionRate + bonusRate).limit(0.0, 1.0)
//        println(rate)
        val cost = (nuclear * rate).coerceAtLeast(0.0)
        nuclear -= cost
        val deltaEnergy = cost * 5.0e8
        val deltaRadiation = radiation * fissionRate
        heat += deltaEnergy * fissionRate + deltaRadiation
        radiation += deltaEnergy * (1 - fissionRate) - deltaRadiation

        val lostRadiation = 0.8 * (1.0 / (slot.temperature * 1000)).halfLifeLossDuring(1.0)
        radiation -= lostRadiation
        slot.reactor.spread(slot, "radiation", lostRadiation)
    }

}