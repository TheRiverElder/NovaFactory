package io.theriverelder.novafactory.builtin.task

import io.theriverelder.novafactory.Game
import io.theriverelder.novafactory.data.getSlot
import io.theriverelder.novafactory.data.task.FactoryTask
import io.theriverelder.novafactory.util.JsonFormattedType
import io.theriverelder.novafactory.util.io.json.JsonNumber
import io.theriverelder.novafactory.util.io.json.JsonObject
import io.theriverelder.novafactory.util.io.json.number
import java.lang.Double.min
import java.lang.Double.parseDouble
import kotlin.math.abs
import kotlin.math.sign

val TYPE_TASK_SET_DEPTH = JsonFormattedType<String, FactoryTask>("setDepth") { SetDepthTask() }

class SetDepthTask(var reactorUid: Int = -1, var slotNumber: Int = -1, var newDepth: Double = -0.0) : FactoryTask(TYPE_TASK_USE) {

    override fun parse(args: List<String>) {
        val reactorUidString = args.getOrNull(0) ?: throw Exception("No args ${args.size}/3")
        val slotNumberString = args.getOrNull(1) ?: throw Exception("No args ${args.size}/3")
        val newDepthString = args.getOrNull(2) ?: throw Exception("No args ${args.size}/3")
        try {
            reactorUid = Integer.parseInt(reactorUidString)
            slotNumber = Integer.parseInt(slotNumberString)
            newDepth = parseDouble(newDepthString)
        } catch (e: Exception) {
            throw Exception("$reactorUid is not a valid number")
        }
    }

    override fun read(json: JsonObject) {
        super.read(json)
        reactorUid = json["reactorUid"].number.toInt()
        slotNumber = json["slotNumber"].number.toInt()
        newDepth = json["newDepth"].number.toDouble()
    }

    override fun write(): JsonObject {
        return super.write().concat(
            "reactorUid" to JsonNumber(reactorUid),
            "slotNumber" to JsonNumber(slotNumber),
            "newDepth" to JsonNumber(newDepth),
        )
    }

    override fun onTick() {
        if (!finished) {
            val slot = Game.factory.getSlot(reactorUid, slotNumber)
            val distance = newDepth - slot.depth
            if (abs(distance) < 0.001) {
                finished = true
            } else {
                val delta = min(abs(distance), 0.02) * sign(distance)
                slot.depth += delta
            }
        }
    }
}