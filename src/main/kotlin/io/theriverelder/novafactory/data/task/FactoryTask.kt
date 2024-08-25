package io.theriverelder.novafactory.data.task

import io.theriverelder.novafactory.interfaces.Tickable
import io.theriverelder.novafactory.util.io.json.*

open class FactoryTask (
    val type: GenericType<String, FactoryTask>,
) : Persistent, Tickable {

    var finished: Boolean = false
        protected set

    open fun parse(args: List<String>) {}

    override fun read(json: JsonObject) {
        finished = json["finished"].boolean
    }

    override fun write(): JsonObject {
        return JsonObject(
            "id" to JsonString(type.key),
            "finished" to jsonBoolean(finished),
        )
    }

    override fun tick() {}
}