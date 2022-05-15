package io.theriverelder.novafactory

import io.theriverelder.novafactory.builtin.BuiltinPlugin
import io.theriverelder.novafactory.builtin.cell.*
import io.theriverelder.novafactory.builtin.history.DataRecord
import io.theriverelder.novafactory.builtin.history.History
import io.theriverelder.novafactory.builtin.server.WebServerPlugin
import io.theriverelder.novafactory.data.NovaFactory
import io.theriverelder.novafactory.data.cell.CellPrototype
import io.theriverelder.novafactory.data.goal.NormalLevel
import io.theriverelder.novafactory.data.reactor.Reactor
import io.theriverelder.novafactory.util.event.EventHandler
import io.theriverelder.novafactory.util.math.ensureLinkedListSizeAbandonHeads
import java.io.File
import java.io.FileWriter
import java.io.Writer

fun initializeTestGame() {
    Game.use(BuiltinPlugin(), WebServerPlugin())
    Game.start(TestLevel(20 * 60 * 1000, 1.5e10))
}

class TestLevel(timeLimit: Long, electricityAmount: Double) : NormalLevel(timeLimit, electricityAmount) {

    override fun setup(factory: NovaFactory) {
        initializeShop(factory)
        prepareTestReactor(factory)
        prepareRecord(factory)
        supplyBeginningRefund(factory)
        supplyPeriodicallyRefund(factory)
    }

    private fun prepareTestReactor(factory: NovaFactory) {
        val reactor = Reactor(factory, 5, 5)
        reactor.breakingTemperature = 4000.0
        reactor.brokenTemperature = 5000.0

        listOf(7, 11, 13, 17).forEach { reactor.getCellSlot(it).cell = CP_NUCLEAR_ROD.create() }
        listOf(6, 18).forEach { reactor.getCellSlot(it).cell = CP_RADIATION_SOURCE.create() }
        reactor.getCellSlot(8).cell = CP_NEUTRON_MIRROR.create()
        reactor.getCellSlot(12).cell = CP_GENERATOR.create()

        factory.reactors.add(reactor)
    }

    private fun prepareRecord(factory: NovaFactory) {
        val fileLogger = FileWriter(File("./powgen.log"))
        val dataRecorder = DataRecorder(10, 10, fileLogger)
        factory.onPostTickHandlers.add(dataRecorder)
    }

    // 初始化商店
    private fun initializeShop(factory: NovaFactory) {
        val shopItemList = createShopItemList()
        factory.shop.addAll(shopItemList)
    }

    private fun createShopItemList(): List<CellPrototype> {
        return listOf(
            CP_SLOWING_ROD,
            CP_NUCLEAR_ROD,
            CP_RADIATION_SOURCE,
            CP_GENERATOR,
            CP_NEUTRON_MIRROR,
            CP_COOLING_BLOCK,
            CP_EXTRA_RADIATION_SOURCE,
        )
    }

    companion object {
        val CP_SLOWING_ROD = CellPrototype("SlowingRod", 5e5) {
            SlowingRodCell {
                slowRate = 0.8
                mass = 1000000.0
                heat = 0.0
                heatTransferFactor = 0.75
                heatCapacity = 1.2
            }
        }

        val CP_NUCLEAR_ROD = CellPrototype("NuclearRod", 5e5) {
            NuclearRodCell {
                fissionRate = 3.0e-5
                nuclear = 100000.0
                nonNuclear = 900000.0
                heatTransferFactor = 0.75
                heatCapacity = 1.2
            }
        }

        val CP_RADIATION_SOURCE = CellPrototype("RadiationSource", 3e5) {
            RadiationSourceCell {
                mass = 1000000.0
                heat = 0.0
                radiationSpeed = 100.0
                heatTransferFactor = 0.75
                heatCapacity = 1.2
            }
        }

        val CP_GENERATOR = CellPrototype("Generator", 1e6) {
            GeneratorCell {
                mass = 1000000.0
                heat = 0.0
                heatTransferFactor = 0.9
//                heatTransferFactor = 0.75
                heatCapacity = 1.5
//                heatCapacity = 1.2
                convertRate = 0.002
            }
        }

        val CP_NEUTRON_MIRROR = CellPrototype("NeutronMirror", 3e3) {
            NeutronMirrorCell {
                mass = 1000000.0
                heat = 0.0
//                heatTransferFactor = 0.75
                heatTransferFactor = 0.4
                heatCapacity = 1.4
            }
        }

        val CP_COOLING_BLOCK = CellPrototype("CoolingBlock", 1e3) {
            CoolingBlockCell {
                mass = 1000000.0
                heat = 0.0
                consumeRate = 0.5
                heatTransferFactor = 0.98
                heatCapacity = 3.0
            }
        }

        val CP_EXTRA_RADIATION_SOURCE = CellPrototype("ExtraRadiationSource", 8e5) {
            RadiationSourceCell {
                mass = 1000000.0
                heat = 0.0
                radiationSpeed = 1e8
                heatTransferFactor = 0.75
                heatCapacity = 1.2
            }
        }
    }

    private fun supplyBeginningRefund(factory: NovaFactory) {
        factory.account += 2e6
    }

    private fun supplyPeriodicallyRefund(factory: NovaFactory) {
        factory.onPostTickHandlers.add(RefundSupplier(100, 1e3))
    }
}

class RefundSupplier(val tickPeriod: Int, val fund: Double) : EventHandler<NovaFactory> {

    private var tickCounter: Int = 0

    override fun handle(factory: NovaFactory) {
        tickCounter++
        if (tickCounter % tickPeriod == 0){
            factory.account += fund
        }
    }
}

class DataRecorder(
    val historyCapacity: Int = 10, // 记录的大小
    val historyPeriodTicks: Int = 10, // 几个tick记录一次
    val fileLogger: Writer? = null, // 文件记录
) : EventHandler<NovaFactory> {

//    private val smoothHistory = LinkedList(history)

    private var tickCounter: Int = 0

    init {
        History.factoryHistory.setWindowToLast(historyCapacity)
    }

    override fun handle(factory: NovaFactory) {
        tickCounter = (tickCounter + 1) % historyPeriodTicks
        if (tickCounter % historyPeriodTicks != 0) return

        val now = Game.time
        val deltaElectricity = factory.electricityGeneratingSpeed

        val history = History.factoryHistory

        history.records.add(DataRecord(now, listOf(deltaElectricity)))
        history.records.ensureLinkedListSizeAbandonHeads(historyCapacity)

        fileLogger?.write("$now\t$deltaElectricity\n")
        fileLogger?.flush()
    }
}
