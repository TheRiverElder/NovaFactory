package io.theriverelder.novafactory.data.goal

import io.theriverelder.novafactory.Game
import io.theriverelder.novafactory.data.NovaFactory
import io.theriverelder.novafactory.data.goal.LevelSuccessAssert.*
import io.theriverelder.novafactory.util.io.json.JsonNumber
import io.theriverelder.novafactory.util.io.json.JsonObject
import io.theriverelder.novafactory.util.io.json.JsonSerializable
import io.theriverelder.novafactory.util.math.shorten
import io.theriverelder.novafactory.util.math.toFixed

open class NormalLevel(
    val timeLimit: Long,
    val electricityAmount: Double,
) : Level {

    var acceptedElectricity: Double = 0.0
    private var successAssert: LevelSuccessAssert = WAITING

    override fun setup(factory: NovaFactory) {
        acceptedElectricity = 0.0
    }

    override fun getSuccessAssert(factory: NovaFactory): LevelSuccessAssert = successAssert

    override fun getInfo(factory: NovaFactory): String {
        val ae = acceptedElectricity
        val ea = electricityAmount
        val t = Game.time
        val tl = timeLimit

        val leftTimeStr = ((tl - t) / 1000).coerceAtLeast(0).shorten()
        val progressStr = "${ae.shorten()} / ${ea.shorten()} EU (${(ae / ea * 100).toFixed(1)}%)"
        val leftElectricity = (ea - ae).coerceAtLeast(0.0).shorten()
        val timeoutStr = ((t - tl) / 1000).coerceAtMost(0).shorten()

        return when (successAssert) {
            WAITING -> "剩余时间：${leftTimeStr} s，已集能量：${progressStr}，还需能量：${leftElectricity} EU"
            SUCCEEDED -> "成功，收集完成：${progressStr}"
            FAILED -> "失败，超时：${timeoutStr} s，收集能量：${progressStr}，还需能量：${leftElectricity} EU"
        }
    }

    // 只有在时限内才接收能量，否则就直接浪费
    override fun input(factory: NovaFactory, electricity: Double) {
        if (Game.time <= timeLimit) {
            acceptedElectricity += electricity
        } else if (successAssert == WAITING) {
            successAssert = if (acceptedElectricity >= electricityAmount) SUCCEEDED else FAILED
        }
        factory.account += electricity * 5e-6
    }

    override fun toJson(): JsonSerializable {
        return JsonObject(
            "timeLimit" to JsonNumber(timeLimit),
            "electricityAmount" to JsonNumber(electricityAmount),
            "acceptedElectricity" to JsonNumber(acceptedElectricity),
            "successAssert" to JsonNumber(successAssert.ordinal),
        )
    }
}