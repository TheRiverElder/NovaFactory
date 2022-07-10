package io.theriverelder.novafactory.builtin

import io.theriverelder.novafactory.Game
import io.theriverelder.novafactory.builtin.cell.*
import io.theriverelder.novafactory.builtin.goal.NormalLevel
import io.theriverelder.novafactory.builtin.history.DataRecord
import io.theriverelder.novafactory.builtin.history.DataSource
import io.theriverelder.novafactory.builtin.history.History
import io.theriverelder.novafactory.builtin.task.*
import io.theriverelder.novafactory.data.NovaFactory
import io.theriverelder.novafactory.data.reactor.Reactor
import io.theriverelder.novafactory.entrance.Plugin
import io.theriverelder.novafactory.util.JsonFormattedType
import io.theriverelder.novafactory.util.event.EventHandler
import io.theriverelder.novafactory.util.math.initializeList
import java.io.File
import java.io.FileWriter
import java.io.Writer

class BuiltinPlugin : Plugin {

    override fun setup() {
        registerCells()
        registerLevels()
        registerTasks()
        prepareRecord(Game.factory)
    }

    // 注册各种Cell
    private fun registerCells() {

        Game.REG_CELL.register(JsonFormattedType(SlowingRodCell::class.simpleName!!) { SlowingRodCell(it) })
        Game.REG_CELL.register(JsonFormattedType(CoolingBlockCell::class.simpleName!!) { CoolingBlockCell(it) })
        Game.REG_CELL.register(JsonFormattedType(GeneratorCell::class.simpleName!!) { GeneratorCell(it) })
        Game.REG_CELL.register(JsonFormattedType(NeutronMirrorCell::class.simpleName!!) { NeutronMirrorCell(it) })
        Game.REG_CELL.register(JsonFormattedType(NuclearRodCell::class.simpleName!!) { NuclearRodCell(it) })
        Game.REG_CELL.register(JsonFormattedType(RadiationSourceCell::class.simpleName!!) { RadiationSourceCell(it) })

    }

    private fun registerLevels() {
        Game.REG_LEVEL.register(JsonFormattedType(NormalLevel::class.simpleName!!) { NormalLevel() })
    }

    private fun registerTasks() {
        Game.REG_TASK.register(TYPE_TASK_BUY)
        Game.REG_TASK.register(TYPE_TASK_SELL)
        Game.REG_TASK.register(TYPE_TASK_PULL)
        Game.REG_TASK.register(TYPE_TASK_USE)
        Game.REG_TASK.register(TYPE_TASK_SET_DEPTH)
        Game.REG_TASK.register(TYPE_TASK_PROGRAM)
    }

    private fun prepareRecord(factory: NovaFactory) {
        val fileLogger = FileWriter(File("./powgen.log"))
        val factoryDataRecorder = FactoryDataRecorder(10, 10, fileLogger)
        factory.onPostTickHandlers.add(factoryDataRecorder)

        val reactorDataRecorder = ReactorDataRecorder(10, 10)
        factory.onReactorPostTickHandlers.add(reactorDataRecorder)
    }
}

class FactoryDataRecorder(
    val historyCapacity: Int = 10, // 记录的大小
    val historyPeriodTicks: Int = 10, // 几个tick记录一次
    val fileLogger: Writer? = null, // 文件记录
) : EventHandler<NovaFactory> {

//    private val smoothHistory = LinkedList(history)

    private var tickCounter: Int = 0

    init {
        History.factoryHistory = DataSource(initializeList(historyCapacity) { DataRecord(0, listOf(0.0)) })
        History.factoryHistory.setWindowToLast(historyCapacity)
    }

    override fun handle(factory: NovaFactory) {
        tickCounter = (tickCounter + 1) % historyPeriodTicks
        if (tickCounter % historyPeriodTicks != 0) return

        val now = Game.time
        val deltaElectricity = factory.lastGenElectricity

        val history = History.factoryHistory

        history.addRecord(DataRecord(now, listOf(deltaElectricity, Game.level?.requirement ?: 0.0)))

        fileLogger?.write("$now\t$deltaElectricity\n")
        fileLogger?.flush()
    }
}

class ReactorDataRecorder(
    val historyCapacity: Int = 10, // 记录的大小
    val historyPeriodTicks: Int = 10, // 几个tick记录一次
) : EventHandler<Reactor> {

//    private val smoothHistory = LinkedList(history)

    private var tickCounter: Int = 0

    override fun handle(reactor: Reactor) {
        tickCounter = (tickCounter + 1) % historyPeriodTicks
        if (tickCounter % historyPeriodTicks != 0) return

        val now = Game.time
        val deltaElectricity = reactor.electricityCache

        val history = History.reactorHistories.getOrPut(reactor.uid) { DataSource(initializeList(historyCapacity) { DataRecord(0, listOf(0.0)) }).also { it.setWindowToLast(historyCapacity) } }

        history.addRecord(DataRecord(now, listOf(deltaElectricity)))
    }
}