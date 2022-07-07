package io.theriverelder.novafactory.builtin.task

import io.theriverelder.novafactory.Game
import io.theriverelder.novafactory.data.task.FactoryTask
import io.theriverelder.novafactory.util.JsonFormattedType
import io.theriverelder.novafactory.util.io.json.JsonNumber
import io.theriverelder.novafactory.util.io.json.JsonObject
import io.theriverelder.novafactory.util.io.json.number
import java.lang.Integer.parseInt

val TYPE_TASK_BUY = JsonFormattedType<String, FactoryTask>("buy") { BuyTask() }

class BuyTask(var shopItemUid: Int = -1) : FactoryTask(TYPE_TASK_BUY) {

    var progress: Double = 0.0

    override fun parse(args: List<String>) {
        val shopItemUidString = args.getOrNull(0) ?: throw Exception("No args ${args.size}/1")
        shopItemUid = try { parseInt(shopItemUidString) } catch (e: Exception) {
            throw Exception("$shopItemUidString is not a valid number")
        }
    }

    override fun read(json: JsonObject) {
        super.read(json)
        shopItemUid = json["shopItemUid"].number.toInt()
    }

    override fun write(): JsonObject {
        return super.write().concat(
            "shopItemUid" to JsonNumber(shopItemUid)
        )
    }

    override fun onTick() {
        if (!finished) {
            progress += 0.1
            if (progress >= 1) {
                finished = true
                Game.factory.buy(shopItemUid)
            }
        }
    }
}