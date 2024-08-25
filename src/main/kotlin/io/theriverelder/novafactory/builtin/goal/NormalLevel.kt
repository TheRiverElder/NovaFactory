package io.theriverelder.novafactory.builtin.goal

import io.theriverelder.novafactory.Game
import io.theriverelder.novafactory.data.NovaFactory
import io.theriverelder.novafactory.data.goal.Level
import io.theriverelder.novafactory.data.goal.LevelSuccessAssert
import io.theriverelder.novafactory.data.goal.LevelSuccessAssert.*
import io.theriverelder.novafactory.util.io.json.JsonNumber
import io.theriverelder.novafactory.util.io.json.JsonObject
import io.theriverelder.novafactory.util.io.json.JsonSerializable
import io.theriverelder.novafactory.util.io.json.number
import kotlin.math.abs

const val TICKS_PER_DAY = 60 * 60 * 24

open class NormalLevel(
    var daytimeConsumption: Double = 0.0,
    var nightConsumption: Double = 0.0,
    override var price: Double = 0.0,
    override var requirement: Double = 0.0
) : Level {

    private lateinit var factory: NovaFactory

    override fun setup(factory: NovaFactory) {
        this.factory = factory
    }

    override fun restore(factory: NovaFactory) {
        this.factory = factory
    }

    override fun getSuccessAssert(factory: NovaFactory): LevelSuccessAssert {
        return WAITING
    }

    override fun toJson(): JsonSerializable {
        return JsonObject(
            "price" to JsonNumber(price),
            "requirement" to JsonNumber(requirement),
        )
    }

    override fun read(json: JsonObject) {
        daytimeConsumption = json["daytimeConsumption"].number.toDouble()
        nightConsumption = json["nightConsumption"].number.toDouble()
        price = json["price"].number.toDouble()
        requirement = json["requirement"].number.toDouble()
    }

    override fun write(): JsonObject {
        return JsonObject(
            "daytimeConsumption" to JsonNumber(daytimeConsumption),
            "nightConsumption" to JsonNumber(nightConsumption),
            "price" to JsonNumber(price),
            "requirement" to JsonNumber(requirement),
        )
    }

    override fun tick() {
        val timeOfDay = Game.time % TICKS_PER_DAY
        requirement = nightConsumption + (daytimeConsumption - nightConsumption) * 2 * abs(timeOfDay / TICKS_PER_DAY.toDouble() - 0.5)
    }
}