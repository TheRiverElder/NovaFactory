package io.theriverelder.novafactory.data.cell

import io.theriverelder.novafactory.data.reactor.CellSlot

data class ValuePack(
    val valueType: String,
    val amount: Double,
    val sourceSlot: CellSlot,
    val targetSlot: CellSlot,
) {
    fun send() {
        targetSlot.receive(this)
    }

    /**
     * 创建一个反向的值包
     */
    fun redirect(newAmount: Double = amount): ValuePack =
        ValuePack(valueType, newAmount, targetSlot, sourceSlot)

    /**
     * 将一部分值（或全部值）返还给源Slot
     */
    fun reject(rejectedPart: Double = amount): ValuePack {
        sourceSlot.accept(redirect(rejectedPart))
        rest -= rejectedPart
        return this
    }

    /**
     * 该数值包是否已经被取消，若被取消，则不会在链式传递中被继续传播
     * */
    var canceled: Boolean = false

    fun cancel(): ValuePack {
        canceled = true
        return this
    }

    /**
     * 剩余的部分
     */
    var rest = amount

    fun consume(consumed: Double): ValuePack {
        rest -= consumed
        return this
    }

    fun consumes(consumed: Double): Double {
        rest -= consumed
        return consumed
    }

    fun consumeAll(): ValuePack = consume(amount)
}
