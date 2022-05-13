package io.theriverelder.novafactory

import io.theriverelder.novafactory.builtin.BuiltinPlugin
import io.theriverelder.novafactory.builtin.cell.*
import io.theriverelder.novafactory.builtin.history.DataRecord
import io.theriverelder.novafactory.builtin.history.History
import io.theriverelder.novafactory.builtin.server.WebServerPlugin
import io.theriverelder.novafactory.data.NovaFactory
import io.theriverelder.novafactory.data.cell.Cell
import io.theriverelder.novafactory.data.cell.CellPrototype
import io.theriverelder.novafactory.data.goal.NormalLevel
import io.theriverelder.novafactory.data.reactor.CellSlot
import io.theriverelder.novafactory.data.reactor.Reactor
import io.theriverelder.novafactory.ui.background.ColorBackground
import io.theriverelder.novafactory.ui.components.*
import io.theriverelder.novafactory.ui.createUI
import io.theriverelder.novafactory.util.event.EventHandler
import io.theriverelder.novafactory.util.explode
import io.theriverelder.novafactory.util.math.*
import io.theriverelder.novafactory.util.task.TaskQueue
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import java.awt.Color
import java.awt.Toolkit
import java.io.File
import java.io.FileWriter
import java.io.Writer
import java.lang.Integer.max
import java.util.*
import kotlin.coroutines.suspendCoroutine

val V_CENTER = VerticalAlign.CENTER
val H_CENTER = HorizontalAlign.CENTER

fun initializeGame() {
    Game.use(BuiltinPlugin(), WebServerPlugin())
    Game.start(ClientTestLevel(20 * 60 * 1000, 1.5e10))
}

lateinit var ui: RootComponent
val taskQueue = TaskQueue<GameTask>()

enum class Brush {
    CLICK,
    NULL,
    USE,
}

fun initializeUI() {

    val width = 1000
    val height = 600
    val screenSize = Toolkit.getDefaultToolkit().screenSize

    val size = 50
    val topBarHeight = 50
    val reactorGap = 10

    var brush: Brush = Brush.CLICK
    var storageIndex = -1

    var watchingCellSlot: CellSlot? = null

    fun sellElectricity() {
        Game.level?.input(Game.factory, Game.factory.output(Game.factory.buttery))
    }

    fun onClickSlot(slot: CellSlot, reactorIndex: Int) {
        if (brush == Brush.CLICK) {
            watchingCellSlot = slot
        } else if (brush == Brush.NULL) {
            val res = Game.factory.use(null, slot)
            println("${res.succeeded}:${res.message}")
            if (res.succeeded && res.extra != null) {
                Game.factory.storage.add(res.extra!!)
            }
        } else {
            val res = Game.factory.use(storageIndex, reactorIndex, slot.number)
            println("${res.succeeded}:${res.message}")
        }
    }

    fun onClickShop(index: Int) {
        val res = Game.factory.buy(index)
        println("${res.succeeded}:${res.message}")
    }

    val root = createUI( (screenSize.width - width) / 2, (screenSize.height - height) / 2, width, height,
        "Hello NovaFactory"
    ) {
        listView(Direction.HORIZONTAL) {
            // 历史记录图表
            listView(Direction.VERTICAL) {
                lineChart(
                    width = width / 2,
                    height = width * 3 / 8,
                    padding = 20,
                    minScale = 1000.0,
                    data = History.factoryHistory,
                )

                // 正在监测的Slot
                val s = watchingCellSlot
                if (s != null) {
                    // Slot的信息
                    val baseText = """
                        Watching: (${s.x}, ${s.y})
                        ${(s.cell?.heat ?: 0.0).shorten()} HU
                        ${s.mass.shorten()} MU
                        ${s.temperature.shorten()} TU
                        ${s.heatCapacity.shorten()} HU/(MU·TU)
                        HTF=${s.heatTransferFactor.toFixed(2)}
                    """.trimIndent().split('\n').joinToString(", ")

                    listView(Direction.VERTICAL) {
                        text(baseText)

                        val c = s.cell
                        if (c == null) { // 如果Slot.cell为空，则提示该Slot的cell为空
                            text("<Empty>")
                        } else { // 如果Slot.cell不为空，则也显示cell的信息
                            text(getCellInfoText(c))
                            text("${c.heat.shorten()} HU, ${c.mass.shorten()} MU")
                        }
                    }
                }
            }

            listView(Direction.VERTICAL) {
                val brushStr: String = when (brush) {
                    Brush.USE -> "<USE>"
                    Brush.NULL -> "<Null>"
                    Brush.CLICK -> "<Click>"
                }
                text("当前时间: ${(Game.time).shortenTime()}",
                    verticalAlign = VerticalAlign.TOP, horizontalAlign = HorizontalAlign.LEFT
                )
                text("""
                    电池余量: ${Game.factory.reactors[0].electricityCache.shorten()} EU
                    笔刷: $brushStr
                    账户余额：${Game.factory.account.shorten()}
                    """.trimIndent().replace("\n", ", "),
                    verticalAlign = VerticalAlign.TOP, horizontalAlign = HorizontalAlign.LEFT
                )
                text(Game.level?.getInfo(Game.factory) ?: "<无关卡>",
                    verticalAlign = VerticalAlign.TOP, horizontalAlign = HorizontalAlign.LEFT
                )

                // 操作按钮
                listView(Direction.HORIZONTAL) {
                    val running = Game.running
                    val bgSwitchOnOff = ColorBackground(if (running) Color.GREEN else Color.RED)
                    box(50, 20, bgSwitchOnOff, onClick = { Game.running = !running }) { text(if (running) "ON" else "OFF") }
                    box(50, 20, ColorBackground(Color.YELLOW), onClick = { printData() }) { text("Print") }
                    box(50, 20, ColorBackground(Color.YELLOW), onClick = { saveData() }) { text("Save") }
                    box(50, 20, ColorBackground(Color.YELLOW), onClick = { sellElectricity() }) { text("Sell") }
                }

                var maxWidth = (Game.factory.shop.size + 2) * size

                // 画笔，包括商店和橡皮擦、监测
                text("笔刷：")
                listView(Direction.HORIZONTAL) {
                    val halfSize = size / 2
                    Brush.values().forEach { b ->
                        box(size, size, ColorBackground(Color.WHITE),
                            borderColor = if (brush == b) Color.BLUE else Color(0, 0, 0, 0),
                            onClick = { brush = b }
                        ) {
                            at(halfSize, halfSize, text("<${b.name.capitalize()}>",
                                verticalAlign = V_CENTER, horizontalAlign = H_CENTER
                            ))
                        }
                    }
                }

                // 商店
                text("商店：")
                listView(Direction.HORIZONTAL) {
                    val halfSize = size / 2
                    for ((index, si) in Game.factory.shop.withIndex()) {
                        box((size * 1.5).toInt(), size, ColorBackground(Color.WHITE), onClick = { onClickShop(index) }) {
                            at(halfSize, halfSize, text(
                                (si.tip.explode(10) + "$${si.price}").joinToString("\n"),
                                verticalAlign = V_CENTER, horizontalAlign = H_CENTER
                            ))
                        }
                    }
                }

                // 显示所有Reactor，但是由于该测试只有一个Reactor，所以整个listView可有可无，这里放着只是以备未来可能的扩展
                listView(Direction.VERTICAL) {
                    var h = topBarHeight + size
                    for ((index, r) in Game.factory.reactors.withIndex()) {
                        val pixelWidth = r.width * size
                        val pixelHeight = r.height * size
                        // 显示单个Reactor
                        box(pixelWidth, pixelHeight) {
                            for (slot in r.getSlots()) {
                                // 显示单个Slot
                                val t = slot.temperature.isNotNaNOr(0.0)
                                val color = if (slot.cell != null) t.toHeatColorInfinity(1 / 1000.0) else Color.BLACK
                                at(slot.x * size, slot.y * size, box(size, size, background = ColorBackground(color),
                                    borderColor = if (slot == watchingCellSlot) Color.WHITE else null,
                                    onClick = { onClickSlot(slot, index) }
                                ) {
                                    at(size / 2, size / 2, text(t.toFixed(2), Color.WHITE, V_CENTER, H_CENTER))
                                })
                            }
                        }
                        h += pixelHeight + reactorGap
                        maxWidth = max(pixelWidth, maxWidth)
                    }
                }

                // 仓库
                listView(Direction.VERTICAL) {
                    for ((index, item) in Game.factory.storage.withIndex()) {
                        box(300, size / 3, ColorBackground(if (storageIndex == index) Color.GRAY else Color.WHITE),
                            onClick = { storageIndex = index }) {
                            text(
                                "【${item.javaClass.simpleName}】",
                                verticalAlign = VerticalAlign.TOP, horizontalAlign = HorizontalAlign.LEFT,
                            )
                        }
                    }
                }
            }
        }
    }

    root.show()

    ui = root
}

fun getCellInfoText(cell: Cell): String {
    return when (cell) {
        is GeneratorCell -> """
            【GeneratorCell】
        """.trimIndent()
        is NuclearRodCell -> """
            【NuclearRodCell】
            half-life: ${cell.fissionRate.shorten()}, 
            nuclear: ${cell.nuclear.shorten()}, 
            non-nuclear: ${cell.nonNuclear.shorten()}, 
            radiation: ${cell.radiation.shorten()}, 
        """.trimIndent()
        is NeutronMirrorCell -> """
            【NeutronMirrorCell】 
        """.trimIndent()
        is RadiationSourceCell -> """
            【RadiationSourceCell】 
            radiationSpeed: ${cell.radiationSpeed.shorten()}, 
        """.trimIndent()
        is CoolingBlockCell -> "【CoolingBlockCell】"
        else -> "【???】"
    }
}

fun printData() {
    val output = StringBuilder()
    Game.toJson().serialize(output)
    println(output)
}

fun saveData() {
    val output = StringBuilder()
    Game.write().serialize(output)
    val writer = FileWriter(File("./save.json"))
    writer.write(output.toString())
    writer.flush()
    writer.close()
}

fun tickClient() {
    Game.tick()
    ui.render()
}

fun main() {
    initializeGame()
    initializeUI()


//    while (true) {
//        delay(50)
//        tickClient()
//    }

    val timer = Timer()
    timer.scheduleAtFixedRate(GameLoop(), 0, 50)
}

class GameLoop : TimerTask() {
    override fun run() {
        taskQueue.enqueueTask(GameTask { tickClient() })
    }
}

class GameTask(val func: suspend () -> Unit) : Runnable {
    override fun run() {
        runBlocking {
            func()
        }
    }
}

class ClientTestLevel(timeLimit: Long, electricityAmount: Double) : NormalLevel(timeLimit, electricityAmount) {

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

//        smoothHistory.add(DataPoint(now / 1000.0, history.sumOf { it.y } / history.size))
//        smoothHistory.ensureLinkedListSizeAbandonHeads(historyCapacity)

        fileLogger?.write("$now\t$deltaElectricity\n")
        fileLogger?.flush()
    }
}