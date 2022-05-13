package io.theriverelder.novafactory.data.cell

import io.theriverelder.novafactory.data.reactor.CellSlot
import io.theriverelder.novafactory.interfaces.HeatObject
import io.theriverelder.novafactory.interfaces.MassObject
import io.theriverelder.novafactory.interfaces.Tickable
import io.theriverelder.novafactory.util.io.json.*

abstract class Cell : Tickable, MassObject, HeatObject, ToJson, Persistent {

    var slot: CellSlot? = null

//    fun request(valuePack: ValuePack) {
//
//    }

    fun receive(valuePack: ValuePack) {
        onReceive(valuePack)
    }

    open fun onReceive(valuePack: ValuePack) {
        if (valuePack.valueType == "heat") {
            val acceptPart = valuePack.amount * heatTransferFactor
            this.heat += acceptPart
            valuePack.consume(acceptPart)
        }
    }

    fun accept(valuePack: ValuePack) {
        onAccept(valuePack)
    }

    open fun onAccept(valuePack: ValuePack) {
        if (valuePack.valueType == "heat") {
            this.heat += valuePack.amount
        }
    }

    fun request(valuePack: ValuePack): ValuePack {
        return onRequest(valuePack)
    }

    abstract fun onRequest(valuePack: ValuePack): ValuePack

    override fun toJson(): JsonSerializable {
        return JsonObject(
            "id" to +this.javaClass.simpleName,
            "heat" to JsonNumber(heat),
            "mass" to JsonNumber(mass),
            "heatCapacity" to JsonNumber(heatCapacity),
            "heatTransferFactor" to JsonNumber(heatTransferFactor),
        )
    }
}