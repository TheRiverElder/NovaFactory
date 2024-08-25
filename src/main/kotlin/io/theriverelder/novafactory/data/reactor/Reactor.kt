package io.theriverelder.novafactory.data.reactor

import io.theriverelder.novafactory.interfaces.Tickable
import io.theriverelder.novafactory.interfaces.Unique
import io.theriverelder.novafactory.util.io.json.Persistent
import io.theriverelder.novafactory.util.io.json.ToJson

interface Reactor : Tickable, ToJson, Persistent, Unique<Int> {

    //#region 状态相关

    // 反应堆是否在运行
    val running: Boolean

    // 反应堆状态，仅作为指示，不应用于实际计算
    val status: ReactorStatus

    // 启动或关闭反应堆
    fun turn(onOrOff: Boolean)

    // 若有单元高于此温度，则反应堆进入融毁中状态
    val breakingTemperature: Double

    // 若有单元格高于此温度，则反应堆进入已融毁状态，并立刻启动自动保护机制，隔绝所有单元的交互
    val brokenTemperature: Double

    // 反应堆的代表温度，一般是其中单元的最高温度
    val temperature: Double

    // 电力缓存
    val electricityCache: Double

    //#endregion 状态相关

    //#region 反应堆规格

    // 反应堆的宽度
    val width: Int

    // 反应堆的高度（对于俯视图来说）
    val height: Int

    // 反应堆可以容纳的单元个数
    val size: Int

    //#endregion 反应堆规格

    //#region 单元槽相关

    // 内容的单元槽列表
    val slots: Collection<CellSlot>

    // 根据序号获取单元槽，有索引越界的隐患，由于序号不一定是连续的或者从0开始的，所以不用get
    fun getCellSlot(number: Int): CellSlot

    // 根据序号获取单元槽，若索引越界则返回null
    fun tryGetCellSlot(number: Int): CellSlot?

    //检查单元序号是否合法
    fun isValidCellSlotNumber(number: Int): Boolean = number in 0 until size

    //#endregion 反应堆规格


}