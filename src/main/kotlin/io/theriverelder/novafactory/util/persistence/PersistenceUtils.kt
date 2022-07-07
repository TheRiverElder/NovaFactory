package io.theriverelder.novafactory.util.persistence

import io.theriverelder.novafactory.Game
import io.theriverelder.novafactory.data.NovaFactory
import io.theriverelder.novafactory.data.cell.Cell
import io.theriverelder.novafactory.data.cell.CellPrototype
import io.theriverelder.novafactory.data.reactor.Reactor
import io.theriverelder.novafactory.data.task.FactoryTask
import io.theriverelder.novafactory.util.io.json.JsonArray
import io.theriverelder.novafactory.util.io.json.JsonObject
import io.theriverelder.novafactory.util.io.json.string
import io.theriverelder.novafactory.util.math.UidGen

fun restoreUidGen(json: JsonObject): UidGen = UidGen().also { it.read(json) }
fun restoreCell(json: JsonObject): Cell = Game.REG_CELL.tryGet(json["id"].string)?.create()?.also { it.read(json) } ?: throw Exception("重建单元失败")
fun restoreTask(json: JsonObject): FactoryTask = Game.REG_TASK.tryGet(json["id"].string)?.create()?.also { it.read(json) } ?: throw Exception("重建任务失败")
fun parseTask(command: String): FactoryTask? {
    val parts = command.trim().split(Regex("\\s+"))
    val head = parts.getOrNull(0) ?: throw Exception("无命令名")
    if (head.isBlank()) return null
    val args = parts.subList(1, parts.size)
    val type = Game.REG_TASK.tryGet(head) ?: throw Exception("未找到名为${head}的命令")
    val task = type.create()
    task.parse(args)
    return task
}
fun restoreCellPrototype(json: JsonObject): CellPrototype {
    val cp = CellPrototype()
    cp.read(json)
    return cp
}
fun restoreReactor(json: JsonObject, factory: NovaFactory): Reactor {
    val reactor = Reactor(factory)
    reactor.read(json)
    return reactor
}