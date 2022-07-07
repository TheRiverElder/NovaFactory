package io.theriverelder.novafactory.builtin.task

import io.theriverelder.novafactory.Game
import io.theriverelder.novafactory.data.task.FactoryTask
import io.theriverelder.novafactory.util.JsonFormattedType
import io.theriverelder.novafactory.util.io.json.JsonNumber
import io.theriverelder.novafactory.util.io.json.JsonObject
import io.theriverelder.novafactory.util.io.json.number
import java.lang.Integer.parseInt

val TYPE_TASK_SELL = JsonFormattedType<String, FactoryTask>("sell") { SellTask() }

class SellTask(var storageItemUid: Int = -1) : FactoryTask(TYPE_TASK_SELL) {

    var progress: Double = 0.0

    override fun parse(args: List<String>) {
        val storageItemUidString = args.getOrNull(0) ?: throw Exception("No args ${args.size}/1")
        storageItemUid = try { parseInt(storageItemUidString) } catch (e: Exception) {
            throw Exception("$storageItemUidString is not a valid number")
        }
    }

    override fun read(json: JsonObject) {
        super.read(json)
        storageItemUid = json["storageItemUid"].number.toInt()
    }

    override fun write(): JsonObject {
        return super.write().concat(
            "storageItemUid" to JsonNumber(storageItemUid),
        )
    }

    override fun onTick() {
        if (!finished) {
            progress += 0.1
            if (progress >= 1) {
                finished = true
                Game.factory.sell(storageItemUid)
            }
        }
    }
}