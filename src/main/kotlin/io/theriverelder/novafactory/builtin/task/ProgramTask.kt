package io.theriverelder.novafactory.builtin.task

import io.theriverelder.novafactory.data.task.FactoryTask
import io.theriverelder.novafactory.util.JsonFormattedType
import io.theriverelder.novafactory.util.io.json.*
import io.theriverelder.novafactory.util.persistence.parseTask
import io.theriverelder.novafactory.util.persistence.restoreTask

val TYPE_TASK_PROGRAM = JsonFormattedType<String, FactoryTask>("program") { ProgramTask() }

class ProgramTask(tasks: List<FactoryTask> = emptyList()) : FactoryTask(TYPE_TASK_PROGRAM) {

    private val tasks = ArrayList<FactoryTask>(tasks)

    override fun parse(args: List<String>) {
        tasks.addAll(args.mapNotNull { parseTask(it) })
    }

    override fun read(json: JsonObject) {
        super.read(json)
        tasks.clear()
        tasks.addAll(json["tasks"].array.map { restoreTask(it.obj) })
    }

    override fun write(): JsonObject {
        return super.write().concat(
            "shopItemUid" to JsonNumber(0),
            "tasks" to JsonArray(tasks.map(FactoryTask::write)),
        )
    }

    override fun tick() {
        val task = tasks.getOrNull(0)
        if (task == null) {
            finished = true
        } else {
            task.tick()
            if (task.finished) {
                tasks.removeFirst()
            }
        }
    }
}