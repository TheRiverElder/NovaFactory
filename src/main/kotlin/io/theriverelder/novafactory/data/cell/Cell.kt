package io.theriverelder.novafactory.data.cell

import io.theriverelder.novafactory.data.reactor.CellSlot
import io.theriverelder.novafactory.interfaces.*
import io.theriverelder.novafactory.util.io.json.*

abstract class Cell(
    val type: GenericType<String, Cell>,
    override var uid: Int = -1,
) : Tickable, MassObject, HeatObject, ToJson, Persistent, Copyable<Cell>, Unique<Int> {

    var slot: CellSlot? = null

    // 用于让单元吸收来自游戏其它来源的数值
    fun receive(valuePack: ValuePack) = onReceive(valuePack)
    open fun onReceive(valuePack: ValuePack) {
        if (valuePack.valueType == "heat") {
            val acceptPart = valuePack.amount * heatTransferFactor
            this.heat += acceptPart
            valuePack.consume(acceptPart)
        }
    }

    // 用于强行让单元吸收数值
    fun accept(valuePack: ValuePack) = onAccept(valuePack)
    open fun onAccept(valuePack: ValuePack) {
        if (valuePack.valueType == "heat") {
            this.heat += valuePack.amount
        }
    }

    fun request(valuePack: ValuePack): ValuePack = onRequest(valuePack)
    open fun onRequest(valuePack: ValuePack): ValuePack {
        TODO()
    }

    override fun toJson(): JsonSerializable {
        return JsonObject(
            "id" to +type.key,
            "uid" to JsonNumber(uid),
            "heat" to JsonNumber(heat),
            "mass" to JsonNumber(mass),
            "heatCapacity" to JsonNumber(heatCapacity),
            "heatTransferFactor" to JsonNumber(heatTransferFactor),
        )
    }

    override fun write(): JsonObject {
        return JsonObject(
            "id" to +type.key,
            "uid" to JsonNumber(uid),
            "heat" to JsonNumber(heat),
            "heatCapacity" to JsonNumber(heatCapacity),
            "heatTransferFactor" to JsonNumber(heatTransferFactor),
        )
    }

    override fun read(json: JsonObject) {
        heat = json["heat"].number.toDouble()
        uid = json["uid"].number.toInt()
    }

    override fun copy(): Cell {
        return type.restore(write())
    }

    override fun onTick() { }
}