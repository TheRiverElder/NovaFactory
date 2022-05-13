package io.theriverelder.novafactory.data.reactor

import io.theriverelder.novafactory.Game
import io.theriverelder.novafactory.data.NovaFactory
import io.theriverelder.novafactory.data.cell.Cell
import io.theriverelder.novafactory.data.cell.ValuePack
import io.theriverelder.novafactory.interfaces.Tickable
import io.theriverelder.novafactory.ui.Vec2
import io.theriverelder.novafactory.util.ActionResult
import io.theriverelder.novafactory.util.io.json.*

class Reactor(val factory: NovaFactory, width: Int = 0, height: Int = 0) : Tickable, ToJson, Persistent {

    var width: Int = width
        private set
    var height: Int = height
        private set
    val size: Int
        get() = width * height

    // 反应堆状态
    var status: ReactorStatus = ReactorStatus.SLEEPING
        private set

    // 若有单元高于此温度，则反应堆进入融毁中状态
    var breakingTemperature: Double = 0.0

    // 若有单元格高于此温度，则反应堆进入已融毁状态，并立刻启动自动保护机制，隔绝所有单元的交互
    var brokenTemperature: Double = 0.0

    // 电力缓存
    var electricityCache: Double = 0.0

    // 反应堆内的单元槽，不包括反应堆外壁这个占位单元槽
    private var slots: Array<CellSlot> = emptyArray()
    val wall: Cell = WallCell()
    val wallSlot: CellSlot = CellSlot(this, -1, 0, 0, wall)

    fun initSlots() {
        slots = Array(width * height) { CellSlot(this, it, it % width, it / height) }
    }

    init {
        initSlots()
    }

    // 启动或关闭反应堆
    fun turn(onOrOff: Boolean): ActionResult<String, Unit> {
        if (onOrOff) {
            return when (status) {
                ReactorStatus.SLEEPING -> {
                    status = ReactorStatus.WORKING
                    ActionResult(true, "启动成功", Unit)
                }
                ReactorStatus.WORKING, ReactorStatus.BREAKING -> ActionResult(false, "反应堆已在运行状态", Unit)
                ReactorStatus.BROKEN -> ActionResult(false, "反应堆已融毁", Unit)
            }
        } else {
            return when (status) {
                ReactorStatus.SLEEPING -> ActionResult(false, "反应堆已在休眠状态", Unit)
                ReactorStatus.WORKING, ReactorStatus.BREAKING -> {
                    status = ReactorStatus.SLEEPING
                    ActionResult(true, "关闭成功", Unit)
                }
                ReactorStatus.BROKEN -> ActionResult(false, "反应堆已融毁", Unit)
            }
        }
    }

    // 根据序号获取单元槽，有索引越界的隐患
    fun getCellSlot(number: Int): CellSlot = slots[number]

    // 根据序号获取单元槽，若索引越界则返回null
    fun tryGetCellSlot(number: Int): CellSlot? = slots.getOrNull(number)

    fun isValidCellSlotNumber(number: Int): Boolean = number >= 0 && number < slots.size

    override fun onTick() {
        liquidAmount = 6000.0
        liquidPerCellCache = liquidAmount / slots.size
        slots.forEach { it.liquidAmount = liquidPerCellCache }

        with(slots.copyOf()) {
            shuffle()
            forEach { it.tick() }
        }

        wall.tick()
        checkStatus()
    }

    // 检查并更新反应堆状态
    protected fun checkStatus() {
        status = if (slots.any { it.temperature >= brokenTemperature }) ReactorStatus.BROKEN
        else if (slots.any { it.temperature >= breakingTemperature }) ReactorStatus.BREAKING
        else ReactorStatus.WORKING
    }

    private val directions = listOf(
        Vec2(1, 0),
        Vec2(-1, 0),
        Vec2(0, 1),
        Vec2(0, -1),
        Vec2(1, 1),
        Vec2(-1, -1),
        Vec2(1, -1),
        Vec2(-1, 1),
    )

    // 传播热量
    fun spread(source: CellSlot, key: String, amount: Double) {
        val part = amount / directions.size
        for (direction in directions) {
            var slot = getSlot(source.x + direction.x, source.y + direction.y)
            var packCancelled = false
            var rest = part
            while (slot != null && !packCancelled) {
                val pack = ValuePack(key, rest, source, slot)
                pack.send()
                packCancelled = pack.canceled
                rest = pack.rest
                slot = getSlot(slot.x + direction.x, slot.y + direction.y)
            }
            wall.accept(ValuePack(key, part, source, wallSlot))
        }
    }

    private fun transferHeat(x1: Int, y1: Int, x2: Int, y2: Int) {
        val slot1 = getSlot(x1, y1)
        val slot2 = getSlot(x2, y2)
        if (slot1 == null || slot2 == null) return

        val h1 = slot1.heat
        val h2 = slot2.heat
        val m1 = slot1.mass
        val m2 = slot2.mass
        val hc1 = slot1.heatCapacity
        val hc2 = slot2.heatCapacity

        val h = h1 + h2
        val m = m1 + m2
        val hca = (hc1 * m1 + hc2 * m2) / m
        val ta = h / (m * hca)
        val deltaHeat = 0.3 * (h1 - (ta * m1 * hca)) * slot1.heatTransferFactor * slot2.heatTransferFactor

        slot1.heat -= deltaHeat
        slot2.heat += deltaHeat
    }

    // 判断一个坐标是否合法而没有越界
    fun isInBound(x: Int, y: Int): Boolean = x in 0 until width && y in 0 until height

    // 根据XY坐标获取单元槽，若越界则返回null
    fun getSlot(x: Int, y: Int): CellSlot? {
        return if (isInBound(x, y)) slots.getOrNull(y * width + x) else null
    }

    // 根据给定序号，获取其相关的单元槽。如果给定的序号越界，则返回空列表。若反应堆处于隔绝状态则也返回空列表
    fun getRelativeSlots(number: Int): List<CellSlot> {
        if (number !in 0 until size) return emptyList()
        if (status == ReactorStatus.BROKEN || status == ReactorStatus.SLEEPING) return emptyList()
        val x = number % width
        val y = number / width
        return setOf(
            getSlot(x - 1, y),
            getSlot(x + 1, y),
            getSlot(x, y - 1),
            getSlot(x, y + 1),
        ).filterNotNull()
    }

    fun getSlots(): Array<CellSlot> {
        return slots
    }

    // 冷却液总量，按体积算
    var liquidAmount: Double = 0.0

    // 缓存当前tick内，每个cell分配到的流体量，按体积算
    var liquidPerCellCache: Double = 0.0

    override fun toJson(): JsonSerializable {
        return JsonObject(
            "size" to JsonNumber(size),
            "width" to JsonNumber(width),
            "height" to JsonNumber(height),
            "slots" to JsonArray(slots.map { it.toJson() }),
        )
    }

    override fun read(json: JsonObject) {
        width = json["width"].number.toInt()
        height = json["height"].number.toInt()
        electricityCache = json["electricityCache"].number.toDouble()
        initSlots()
        val cells: List<Cell?> = json["cells"].array
            .map { it.obj }
            .map { cellJson -> Game.REG_CELL.tryGet(cellJson["id"].string)?.create()?.also { read(cellJson) } }
        for ((number, slot) in slots.withIndex()) {
            slot.cell = cells[number]
        }
    }

    override fun write(): JsonObject {
        return JsonObject(
            "width" to JsonNumber(width),
            "height" to JsonNumber(height),
            "electricityCache" to JsonNumber(electricityCache),
            "cells" to JsonArray(slots.map { it.cell }.map { it?.write() ?: JSON_NULL }),
        )
    }

}

enum class ReactorStatus {
    // 反应堆的默认状态，在发电产开始工作时就会转为WORKING
    SLEEPING,
    // 反应堆正常工作的状态
    WORKING,
    // 反应堆正在融毁，诱发原因是反应堆存在高于融毁中温度的单元。此时反应堆还会继续工作，但是会有警报
    BREAKING,
    // 反应堆正在融毁，诱发原因是反应堆存在高于已融毁温度的单元。此时反应堆的单元继续工作，但是都被隔离，且此反应堆永远无法被使用
    BROKEN,
    ;
}