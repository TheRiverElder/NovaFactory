package io.theriverelder.novafactory.builtin.cell

import io.theriverelder.novafactory.data.cell.Cell
import io.theriverelder.novafactory.data.cell.ValuePack
import io.theriverelder.novafactory.util.io.json.*
import io.theriverelder.novafactory.util.math.clamp
import kotlin.math.pow

val ENERGY_PER_MASS = 299792458.0.pow(2)

class NuclearRodCell(type: GenericType<String, Cell>, init: NuclearRodCell.() -> Unit = {}) : Cell(type) {

    // 当一单位中子成功引发裂变，中子会变成原来的几倍，这是一个常量, it > 1
    public var multiplier: Double = 1.0
    // 当一单位中子成功引发裂变，会有多少核质量变为能量，这是一个常量, it > 0
    public var fissionCost: Double = 1.0
    // 自发产生的质量占核质量之比, 0 < it < 1
    public var fissionRatio: Double = 1.0
    // 0 < chanceToFission + chanceToSlow + chanceToEscape < 1
    // 成功引发裂变的概率, 0 < it < 1
    public var chanceToHit: Double = 1.0
    // 中子减速的概率，减速后中子消失，按照核质量与非核质量变成对应的质量, 0 < it < 1
    public var chanceToSlow: Double = 1.0
    // 中子逃离的几率，中子有几率逃离该单元并分散到其它单元, 0 < it < 1
    public var chanceToEscape: Double = 1.0
    // 当前的自由中子的质量, it > 0
    public var neutron: Double = 0.0
    // 可以产生裂变的物质的质量
    public var nuclear: Double = 0.0
    // 不参与反应的惰性物质的质量
    public var nonNuclear: Double = 0.0

    override val mass: Double
        get() = nuclear + nonNuclear
    override var heat: Double = 0.0
    override var heatTransferFactor: Double = 0.0
    override var heatCapacity: Double = 1.0

    override fun toJson(): JsonSerializable {
        return (super.toJson() as JsonObject).concat(
            "nuclear" to JsonNumber(nuclear),
            "nonNuclear" to JsonNumber(nonNuclear),
            "neutron" to JsonNumber(neutron),
        )
    }

    override fun read(json: JsonObject) {
        super.read(json)
        heatTransferFactor = json["heatTransferFactor"].number.toDouble()
        heatCapacity = json["heatCapacity"].number.toDouble()

        multiplier = json["multiplier"].number.toDouble()
        fissionCost = json["fissionCost"].number.toDouble()
        fissionRatio = json["fissionRatio"].number.toDouble()
        chanceToHit = json["chanceToHit"].number.toDouble()
        chanceToSlow = json["chanceToSlow"].number.toDouble()
        chanceToEscape = json["chanceToEscape"].number.toDouble()
        neutron = json["neutron"].number.toDouble()
        nuclear = json["nuclear"].number.toDouble()
        nonNuclear = json["nonNuclear"].number.toDouble()
    }

    override fun write(): JsonObject {
        return super.write().concat(
            "multiplier" to JsonNumber(multiplier),
            "fissionCost" to JsonNumber(fissionCost),
            "fissionRatio" to JsonNumber(fissionRatio),
            "chanceToHit" to JsonNumber(chanceToHit),
            "chanceToSlow" to JsonNumber(chanceToSlow),
            "chanceToEscape" to JsonNumber(chanceToEscape),
            "neutron" to JsonNumber(neutron),
            "nuclear" to JsonNumber(nuclear),
            "nonNuclear" to JsonNumber(nonNuclear),
        )
    }

    init {
        init()
    }

    override fun receive(valuePack: ValuePack) {
        when (valuePack.valueType) {
            "neutron" -> accept(valuePack)
            else -> super.receive(valuePack)
        }
    }

    override fun accept(valuePack: ValuePack) {
        when (valuePack.valueType) {
            "neutron" -> {
                neutron += valuePack.amount
                valuePack.consumeAll()
            }
            else -> super.accept(valuePack)
        }
    }

    override fun tick() {
        val slot = slot!!

        val naturalFission = (nuclear * fissionRatio * slot.depth).clamp(0.0, nuclear)
        nuclear -= naturalFission
        neutron += naturalFission

        val neutron = this.neutron * slot.depth
        val neutronToHit = neutron * chanceToHit
        val neutronToSlow = neutron * chanceToSlow
        val neutronToEscape = neutron * chanceToEscape
        this.neutron -= neutronToHit + neutronToSlow + neutronToEscape

        val nuclearCostForNewNeutron = (neutronToHit * (multiplier - 1)).coerceAtMost(nuclear)
        nuclear -= nuclearCostForNewNeutron
        this.neutron += nuclearCostForNewNeutron
        val nuclearCostForEnergy = (neutronToHit * fissionCost).coerceAtMost(nuclear)
        nuclear -= nuclearCostForEnergy
        heat += nuclearCostForEnergy * ENERGY_PER_MASS

        val ratioToHeatForSlowedNeutron = fissionCost
        heat += ratioToHeatForSlowedNeutron * neutronToSlow
        nonNuclear += (1 - ratioToHeatForSlowedNeutron) * neutronToSlow

        slot.reactor.spread(slot, "neutron", neutronToEscape)
    }

}