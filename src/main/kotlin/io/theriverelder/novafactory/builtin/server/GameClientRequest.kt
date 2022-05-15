package io.theriverelder.novafactory.builtin.server

import io.theriverelder.novafactory.Game
import io.theriverelder.novafactory.builtin.history.History
import io.theriverelder.novafactory.util.ActionResult
import io.theriverelder.novafactory.util.Creator
import io.theriverelder.novafactory.util.io.json.*
import io.theriverelder.novafactory.util.registry.Registry

val REG_GAME_CLIENT_REQUEST = Registry<String, Creator<String, GameClientRequest>> { it.key }

abstract class GameClientRequest(
    val id: String,
) {
    fun response(): JsonSerializable {
        val res = process()
        return JsonObject(
            "id" to JsonString(id),
            "succeeded" to jsonBoolean(res.succeeded),
            "message" to JsonString(res.message),
            "data" to (res.extra ?: JSON_NULL),
        )
    }

    abstract fun process(): ActionResult<String, JsonSerializable?>
}

class ReactorInfoRequest(
    val index: Int,
) : GameClientRequest("reactor_info") {
    override fun process(): ActionResult<String, JsonSerializable?> {
        val reactor = Game.factory.reactors.getOrNull(index)
            ?: return ActionResult(false, "No reactor with index $index", null)
        return ActionResult(true, "", reactor.toJson())
    }
}

class ReactorHistoryRequest(
    val index: Int,
) : GameClientRequest("reactor_history") {
    override fun process(): ActionResult<String, JsonSerializable?> {
        val reactorHistory = History.reactorHistories.getOrNull(index)
            ?: return ActionResult(false, "No reactor with index $index", null)
        return ActionResult(true, "", JsonArray(reactorHistory.getSlice().map { it.toJson() }))
    }
}

class FactoryInfoRequest : GameClientRequest("factory_info") {
    override fun process(): ActionResult<String, JsonSerializable?> {
        return ActionResult(true, "", Game.factory.toInfoJson())
    }
}

class FactoryHistoryRequest : GameClientRequest("factory_history") {
    override fun process(): ActionResult<String, JsonSerializable?> {
        val factoryHistory = History.factoryHistory
        return ActionResult(true, "", JsonArray(factoryHistory.getSlice().map { it.toJson() }))
    }
}