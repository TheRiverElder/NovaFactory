package io.theriverelder.novafactory

import io.theriverelder.novafactory.builtin.BuiltinPlugin
import io.theriverelder.novafactory.builtin.cell.*
import io.theriverelder.novafactory.builtin.goal.NormalLevel
import io.theriverelder.novafactory.builtin.server.WebServerPlugin
import io.theriverelder.novafactory.data.NovaFactory
import io.theriverelder.novafactory.data.cell.CellPrototype
import io.theriverelder.novafactory.data.reactor.RectangleReactor
import io.theriverelder.novafactory.util.event.EventHandler

fun initializeTestGame() {
    Game.use(BuiltinPlugin(), WebServerPlugin())
    Game.start(TestLevel(5.5e5, 3.5e5, 0.03))
}

class TestLevel(daytimeConsumption: Double, nightConsumption: Double, price: Double) :
    NormalLevel(daytimeConsumption, nightConsumption, price) {

    override fun setup(factory: NovaFactory) {
        super.setup(factory)
        initializeShop(factory)
        prepareTestReactor(factory)
        supplyBeginningRefund(factory)
        supplyPeriodicallyRefund(factory)
    }

    private fun prepareTestReactor(factory: NovaFactory) {
        val reactor = RectangleReactor(factory, 5, 5)
        reactor.uid = factory.reactorUidGen.gen()
        reactor.breakingTemperature = 2000.0
        reactor.brokenTemperature = 3000.0

        listOf(7, 11, 13, 17).forEach { reactor.getCellSlot(it).cell = CP_NUCLEAR_ROD.create() }
//        listOf(6, 18).forEach { reactor.getCellSlot(it).cell = CP_RADIATION_SOURCE.create() }
        reactor.getCellSlot(8).cell = CP_NEUTRON_MIRROR.create()
        reactor.getCellSlot(12).cell = CP_GENERATOR.create()

        factory.addReactor(reactor)
    }

    // 初始化商店
    private fun initializeShop(factory: NovaFactory) {
        val shopItemList = createShopItemList(factory)
        factory.shop.addAll(shopItemList)
    }

    private fun createShopItemList(factory: NovaFactory): List<CellPrototype> {
        return listOf(
            CP_SLOWING_ROD,
            CP_NUCLEAR_ROD,
            CP_RADIATION_SOURCE,
            CP_GENERATOR,
            CP_NEUTRON_MIRROR,
            CP_COOLING_BLOCK,
            CP_EXTRA_RADIATION_SOURCE,
        ).onEach { it.uid = factory.shopItemUidGen.gen() }
    }

    companion object {
        val CP_SLOWING_ROD = CellPrototype(5e5,
            SlowingRodCell(Game.REG_CELL["SlowingRodCell"]) {
                slowRate = 0.8
                mass = 1000000.0
                heat = 0.0
                heatTransferFactor = 0.75
                heatCapacity = 1.2
            }
        )

        val CP_NUCLEAR_ROD = CellPrototype(5e5,
            NuclearRodCell(Game.REG_CELL["NuclearRodCell"]) {
                multiplier = 2.5
                fissionCost = 1.112650056e-6
                fissionRatio = 2.196450843e-8
                chanceToHit = 0.3
                chanceToSlow = 0.1
                chanceToEscape = 0.6
                nuclear = 100000.0
                nonNuclear = 900000.0
                heatTransferFactor = 0.75
                heatCapacity = 1.2
            }
        )

        val CP_RADIATION_SOURCE = CellPrototype(3e5,
            RadiationSourceCell(Game.REG_CELL["RadiationSourceCell"]) {
                mass = 1000000.0
                heat = 0.0
                radiationSpeed = 100.0
                heatTransferFactor = 0.75
                heatCapacity = 1.2
            }
        )

        val CP_GENERATOR = CellPrototype(1e6,
            GeneratorCell(Game.REG_CELL["GeneratorCell"]) {
                mass = 1000000.0
                heat = 0.0
                heatTransferFactor = 0.9
//                heatTransferFactor = 0.75
                heatCapacity = 1.5
//                heatCapacity = 1.2
                convertRate = 0.002
            }
        )

        val CP_NEUTRON_MIRROR = CellPrototype(3e3,
            NeutronMirrorCell(Game.REG_CELL["NeutronMirrorCell"]) {
                mass = 1000000.0
                heat = 0.0
//                heatTransferFactor = 0.75
                heatTransferFactor = 0.4
                heatCapacity = 1.4
            }
        )

        val CP_COOLING_BLOCK = CellPrototype(1e3,
            CoolingBlockCell(Game.REG_CELL["CoolingBlockCell"]) {
                mass = 1000000.0
                heat = 0.0
                consumeRate = 0.5
                heatTransferFactor = 0.98
                heatCapacity = 3.0
            }
        )

        val CP_EXTRA_RADIATION_SOURCE = CellPrototype(8e5,
            RadiationSourceCell(Game.REG_CELL["RadiationSourceCell"]) {
                mass = 1000000.0
                heat = 0.0
                radiationSpeed = 1e8
                heatTransferFactor = 0.75
                heatCapacity = 1.2
            }
        )
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
        if (tickCounter % tickPeriod == 0) {
            factory.account += fund
        }
    }
}
