package io.theriverelder.novafactory

import io.theriverelder.novafactory.builtin.cell.*
import io.theriverelder.novafactory.builtin.history.History
import io.theriverelder.novafactory.data.cell.Cell
import io.theriverelder.novafactory.data.reactor.CellSlot
import io.theriverelder.novafactory.ui.background.ColorBackground
import io.theriverelder.novafactory.ui.components.*
import io.theriverelder.novafactory.ui.createUI
import io.theriverelder.novafactory.util.event.EventHandler
import io.theriverelder.novafactory.util.explode
import io.theriverelder.novafactory.util.math.*
import io.theriverelder.novafactory.util.wrap
import java.awt.Color
import java.awt.Toolkit
import java.io.File
import java.io.FileWriter
import java.lang.Integer.max

val V_CENTER = VerticalAlign.CENTER
val H_CENTER = HorizontalAlign.CENTER


enum class Brush {
    CLICK,
    PULL,
    USE,
}

fun initializeUI(): RootComponent {

    val width = 1000
    val height = 600
    val screenSize = Toolkit.getDefaultToolkit().screenSize

    val size = 50
    val topBarHeight = 50
    val reactorGap = 10

    var brush: Brush = Brush.CLICK
    var storageItemUid = -1

    var watchingCellSlot: CellSlot? = null

    fun onClickSlot(slot: CellSlot, reactorUid: Int) {
        when (brush) {
            Brush.CLICK -> watchingCellSlot = slot
            Brush.PULL -> {
                val res = wrap {
                    slot.depth = 0.0
                    Game.factory.pull(reactorUid, slot.number)
                }
                println("${res.succeeded}:${res.message}")
            }
            else -> {
                val res = wrap { Game.factory.use(reactorUid, slot.number, storageItemUid) }
                if (res.succeeded) {
                    slot.depth = 1.0
                }
                println("${res.succeeded}:${res.message}")
            }
        }
    }

    fun onClickShop(index: Int) {
        val res = wrap { Game.factory.buy(index) }
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
                    Brush.PULL -> "<PULL>"
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

                // 操作按钮
                listView(Direction.HORIZONTAL) {
                    val running = Game.running
                    val bgSwitchOnOff = ColorBackground(if (running) Color.GREEN else Color.RED)
                    box(50, 20, bgSwitchOnOff, onClick = { Game.running = !running }) { text(if (running) "ON" else "OFF") }
                    box(50, 20, ColorBackground(Color.YELLOW), onClick = { printData() }) { text("Print") }
                    box(50, 20, ColorBackground(Color.YELLOW), onClick = { saveData() }) { text("Save") }
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
                                (si.name.explode(10) + "$${si.price}").joinToString("\n"),
                                verticalAlign = V_CENTER, horizontalAlign = H_CENTER
                            ))
                        }
                    }
                }

                // 显示所有Reactor，但是由于该测试只有一个Reactor，所以整个listView可有可无，这里放着只是以备未来可能的扩展
                listView(Direction.VERTICAL) {
                    var h = topBarHeight + size
                    for ((index, r) in Game.factory.reactors.withIndex()) {
                        box(300, 20) {
                            at(0, 10, text("Reactor #$index, status=${r.status.name}", verticalAlign = VerticalAlign.CENTER, horizontalAlign = HorizontalAlign.LEFT))
                            at(200, 0, box(100, 20, ColorBackground(if (r.running) Color.GREEN else Color.RED), onClick = { r.turn(!r.running) }) {
                                at(0, 10, text(if (r.running) "关闭" else "启动", verticalAlign = VerticalAlign.CENTER, horizontalAlign = HorizontalAlign.LEFT))
                            })
                        }
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
                                    onClick = { onClickSlot(slot, r.uid) }
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
                        box(300, size / 3, ColorBackground(if (storageItemUid == item.uid) Color.GRAY else Color.WHITE),
                            onClick = { storageItemUid = item.uid }) {
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

    return root
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

fun getCellInfoText(cell: Cell): String {
    return when (cell) {
        is GeneratorCell -> """
            【GeneratorCell】
        """.trimIndent()
        is NuclearRodCell -> """
            【NuclearRodCell】
            multiplier: ${cell.multiplier.shorten()}, 
            fissionCost: ${cell.fissionCost.shorten(5)}, 
            fissionRatio: ${cell.fissionRatio.shorten(10)}, 
            chanceToHit: ${cell.chanceToHit.shorten()}, 
            chanceToSlow: ${cell.chanceToSlow.shorten()}, 
            chanceToEscape: ${cell.chanceToEscape.shorten()}, 
            nuclear: ${cell.nuclear.shorten(5)}, 
            non-nuclear: ${cell.nonNuclear.shorten(5)}, 
            neutron: ${cell.neutron.shorten(5)}, 
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

class RenderOnTick(private val ui: RootComponent) : EventHandler<Game> {
    override fun handle(event: Game) {
        ui.render()
    }
}

fun main() {
    initializeTestGame()
    Game.onPostTickHandlers.add(RenderOnTick(initializeUI()))
    GameLifeCycle.runGame()
}
