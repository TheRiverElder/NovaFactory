package io.theriverelder.novafactory.data.reactor;

import io.theriverelder.novafactory.data.cell.Cell
import io.theriverelder.novafactory.data.cell.ValuePack
import io.theriverelder.novafactory.interfaces.HeatObject
import io.theriverelder.novafactory.interfaces.MassObject
import io.theriverelder.novafactory.interfaces.Tickable
import io.theriverelder.novafactory.util.io.json.*
import io.theriverelder.novafactory.util.math.ease0to1
import io.theriverelder.novafactory.util.math.isNotNaNOr
import io.theriverelder.novafactory.util.math.clamp

class CellSlot(
    val reactor: Reactor,
    val number: Int,
    val x: Int,
    val y: Int,
    cell: Cell? = null,
) : MassObject, HeatObject, Tickable, ToJson {

    var cell: Cell? = cell
        set(value) {
            field?.slot = null
            value?.slot = this
            field = value
        }

    val temperature: Double
        get() {
            val h = heat + (cell?.heat ?: 0.0)
            if (h == 0.0) return 0.0
            val m = mass + (cell?.mass ?: 0.0)
            return if (mass == 0.0) Double.POSITIVE_INFINITY else h / m * heatCapacity
        }

    override var heat: Double
        get() = cell?.heat ?: 0.0
        set(value) {
            val cell = cell
            if (cell != null) {
                cell.heat = value
            }
        }

    override val mass: Double
        get() = cell?.mass ?: 0.0

    override val heatTransferFactor: Double get() = cell?.heatTransferFactor ?: 0.0

    override val heatCapacity: Double
        get() = cell?.heatCapacity ?: 1.0

    // 插入的深度，一般作用于燃料棒，范围从0%到100%，只有插入的部分会参与反应
    var depth: Double = 1.0

    var liquidAmount: Double = 0.0

    override fun onTick() {
        val cell = this.cell
        if (cell != null) {
            cell.slot = this
            cell.tick()
            spreadHeat()

            if (liquidAmount > 0) {
                if (temperature > 1000.0) {
                    val liquidLoss = liquidAmount.coerceAtMost(100.0)
                    liquidAmount -= liquidLoss
                    val deltaHeat = liquidLoss * 100.0
                    cell.heat -= deltaHeat
                    reactor.electricityCache += deltaHeat
                } else {
                    cell.heat -= cell.heat * 1e-3
                }
            }
        }
    }

    val relativeSlots: List<CellSlot> get() = reactor.getRelativeSlots(number)

    fun receive(valuePack: ValuePack) = cell?.receive(valuePack)

    fun accept(valuePack: ValuePack) = cell?.accept(valuePack)



    fun request(valuePack: ValuePack): ValuePack = cell?.request(valuePack) ?: valuePack.redirect(0.0)

    fun spreadHeat() {
        cell ?: return

        val lostRate = (heatTransferFactor * 0.3 * temperature.ease0to1(1 / 1.0e3)).clamp(0.0, 1.0)
//        val lostRate = 1.0
        val lostHeat = heat * lostRate
//        val id = cell!!.javaClass.simpleName
//        println(id)
//        when (id) {
//            "NuclearRodCell" -> println("NuclearRodCell send ${lostRate.toFixed(2)}")
//            "GeneratorCell" -> println("GeneratorCell send ${lostRate.toFixed(2)}")
//        }
//        heat -= partHeat * relativeSlots.size
        heat -= lostHeat

        reactor.spread(this, "heat", lostHeat)
    }

    override fun toJson(): JsonSerializable {
        return JsonObject(
            "number" to JsonNumber(number),
            "x" to JsonNumber(x),
            "y" to JsonNumber(y),
            "temperature" to JsonNumber(temperature.isNotNaNOr(0.0)),
            "cell" to (cell?.toJson() ?: JSON_NULL),
            "depth" to JsonNumber(depth),
        )
    }

    override fun equals(other: Any?): Boolean {
        return this.number == (other as? CellSlot)?.number
    }
}
