package io.theriverelder.novafactory.builtin.task

import io.theriverelder.novafactory.Game
import io.theriverelder.novafactory.data.task.FactoryTask
import io.theriverelder.novafactory.util.JsonFormattedType
import io.theriverelder.novafactory.util.io.json.JsonNumber
import io.theriverelder.novafactory.util.io.json.JsonObject
import io.theriverelder.novafactory.util.io.json.number

val TYPE_TASK_PULL = JsonFormattedType<String, FactoryTask>("pull") { PullTask() }

class PullTask(var reactorUid: Int = -1, var slotNumber: Int = -1) : FactoryTask(TYPE_TASK_PULL) {

    var progress: Double = 0.0

    override fun parse(args: List<String>) {
        val reactorUidString = args.getOrNull(0) ?: throw Exception("No args ${args.size}/2")
        val slotNumberString = args.getOrNull(1) ?: throw Exception("No args ${args.size}/2")
        try {
            reactorUid = Integer.parseInt(reactorUidString)
            slotNumber = Integer.parseInt(slotNumberString)
        } catch (e: Exception) {
            throw Exception("$reactorUid is not a valid number")
        }
    }

    override fun read(json: JsonObject) {
        super.read(json)
        reactorUid = json["reactorUid"].number.toInt()
        slotNumber = json["slotNumber"].number.toInt()
    }

    override fun write(): JsonObject {
        return super.write().concat(
            "reactorUid" to JsonNumber(reactorUid),
            "slotNumber" to JsonNumber(slotNumber),
        )
    }

    override fun onTick() {
        if (!finished) {
            progress += 0.1
            if (progress >= 1) {
                finished = true
                Game.factory.pull(reactorUid, slotNumber)
            }
        }
    }
}