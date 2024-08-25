package io.theriverelder.novafactory.data.reactor

import io.theriverelder.novafactory.data.NovaFactory
import io.theriverelder.novafactory.data.cell.Cell
import io.theriverelder.novafactory.data.cell.ValuePack
import io.theriverelder.novafactory.interfaces.Tickable
import io.theriverelder.novafactory.ui.Vec2
import io.theriverelder.novafactory.util.io.json.*
import io.theriverelder.novafactory.util.persistence.restoreCell

class RectangleReactor(val factory: NovaFactory, width: Int = 0, height: Int = 0) : Reactor {

    override var uid: Int = -1
    private var isolated: Boolean = false

    override var running: Boolean = false; private set
    override var width: Int = width; private set
    override var height: Int = height; private set
    override val size: Int get() = slots.size
    override val slots: Collection<CellSlot>; get() = containingSlots.toList()
    override val temperature: Double get() = if (containingSlots.isEmpty()) 0.0 else containingSlots.maxOf { it.temperature }
    override var breakingTemperature: Double = 0.0
    override var brokenTemperature: Double = 0.0

    override val status: ReactorStatus
        get() = (if (isolated) ReactorStatus.BROKEN else if (running) ReactorStatus.WORKING else ReactorStatus.SLEEPING)

    // 电力缓存
    override var electricityCache: Double = 0.0

    // 反应堆内的单元槽，不包括反应堆外壁这个占位单元槽
    private var containingSlots: Array<CellSlot> = Array(width * height) { CellSlot(this, it, it % width, it / height) }
    private var wall: Cell = WallCell()
    private var wallSlot: CellSlot = CellSlot(this, -1, 0, 0, wall)

    override fun turn(onOrOff: Boolean) {
        if (isolated) throw Exception("反应堆已融毁")
        if (onOrOff == running) throw Exception("反应堆已处于目标状态")
        running = onOrOff
    }

    override fun getCellSlot(number: Int): CellSlot = containingSlots[number]
    override fun tryGetCellSlot(number: Int): CellSlot? = containingSlots.getOrNull(number)

    // 判断一个坐标是否合法而没有越界
    private fun isInBound(x: Int, y: Int): Boolean = x in 0 until width && y in 0 until height

    // 根据XY坐标获取单元槽，若越界则返回null
    private fun getSlot(x: Int, y: Int): CellSlot? {
        return if (isInBound(x, y)) containingSlots.getOrNull(y * width + x) else null
    }

    override fun tick() {

        if (running) {
            // 重新注入冷却液到指定数量
            val liquidAmount = 6000.0
            // 当前tick内，每个cell分配到的流体量，按体积算
            val liquidPerCellCache = liquidAmount / containingSlots.size
            // 将冷却液分配到每个单元
            containingSlots.forEach { it.liquidAmount = liquidPerCellCache }

            with(containingSlots.copyOf()) {
                shuffle()
                forEach(Tickable::tick)
            }
        }

        wall.tick()
    }

    // 传播数值
    fun spread(source: CellSlot, key: String, amount: Double) {
        if (isolated) return

        val part = amount / DIRECTIONS.size
        for (direction in DIRECTIONS) {
            var rest = part
            var slot = getSlot(source.x + direction.x, source.y + direction.y)
            var packCancelled = false
            while (slot != null && !packCancelled) {
                val pack = ValuePack(key, rest * slot.depth, source, slot)
                pack.send()
                packCancelled = pack.canceled
                rest = pack.rest
                slot = getSlot(slot.x + direction.x, slot.y + direction.y)
            }
            wall.accept(ValuePack(key, rest, source, wallSlot))
        }
    }

    // 老算法暂时保留
//    private fun transferHeat(x1: Int, y1: Int, x2: Int, y2: Int) {
//        val slot1 = getSlot(x1, y1)
//        val slot2 = getSlot(x2, y2)
//        if (slot1 == null || slot2 == null) return
//
//        val h1 = slot1.heat
//        val h2 = slot2.heat
//        val m1 = slot1.mass
//        val m2 = slot2.mass
//        val hc1 = slot1.heatCapacity
//        val hc2 = slot2.heatCapacity
//
//        val h = h1 + h2
//        val m = m1 + m2
//        val hca = (hc1 * m1 + hc2 * m2) / m
//        val ta = h / (m * hca)
//        val deltaHeat = 0.3 * (h1 - (ta * m1 * hca)) * slot1.heatTransferFactor * slot2.heatTransferFactor
//
//        slot1.heat -= deltaHeat
//        slot2.heat += deltaHeat
//    }

    // 根据给定序号，获取其相关的单元槽。如果给定的序号越界，则返回空列表。若反应堆处于隔绝状态则也返回空列表
    fun getRelativeSlots(number: Int): List<CellSlot> {
        if (!isValidCellSlotNumber(number)) return emptyList()
        if (status.isolated) return emptyList()
        val x = number % width
        val y = number / width
        return DIRECTIONS.mapNotNull { getSlot(x + it.x, y + it.y) }
    }

    fun getSlots(): Array<CellSlot> {
        return containingSlots.copyOf()
    }

    override fun toJson(): JsonSerializable {
        return JsonObject(
            "uid" to JsonNumber(uid),
            "size" to JsonNumber(size),
            "width" to JsonNumber(width),
            "height" to JsonNumber(height),
            "slots" to JsonArray(containingSlots.map { it.toJson() }),
            "status" to JsonString(status.name),
            "running" to JsonBoolean(running),
            "temperature" to JsonNumber(temperature),
            "breakingTemperature" to JsonNumber(breakingTemperature),
            "brokenTemperature" to JsonNumber(brokenTemperature),
        )
    }

    override fun read(json: JsonObject) {
        uid = json["uid"].number.toInt()
        width = json["width"].number.toInt()
        height = json["height"].number.toInt()
        breakingTemperature = json["breakingTemperature"].number.toDouble()
        brokenTemperature = json["brokenTemperature"].number.toDouble()
        electricityCache = json["electricityCache"].number.toDouble()
        containingSlots = json["cells"].array
            .map { restoreCell(it.obj) }
            .withIndex()
            .map { CellSlot(this, it.index, it.index % width, it.index / height, it.value) }
            .toTypedArray()
        wall = WallCell{ read(json["wall"].obj) }
        wallSlot = CellSlot(this, -1, -1, -1, wall)
    }

    override fun write(): JsonObject {
        return JsonObject(
            "uid" to JsonNumber(uid),
            "width" to JsonNumber(width),
            "height" to JsonNumber(height),
            "breakingTemperature" to JsonNumber(breakingTemperature),
            "brokenTemperature" to JsonNumber(brokenTemperature),
            "electricityCache" to JsonNumber(electricityCache),
            "cells" to JsonArray(containingSlots.map { it.cell }.map { it?.write() ?: JSON_NULL }),
            "wall" to wall.write(),
        )
    }

    companion object {
        private val DIRECTIONS = listOf(
            Vec2(1, 0),
            Vec2(-1, 0),
            Vec2(0, 1),
            Vec2(0, -1),
            Vec2(1, 1),
            Vec2(-1, -1),
            Vec2(1, -1),
            Vec2(-1, 1),
        )
    }

}

