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
            // ??????????????????
            listView(Direction.VERTICAL) {
                lineChart(
                    width = width / 2,
                    height = width * 3 / 8,
                    padding = 20,
                    minScale = 1000.0,
                    data = History.factoryHistory,
                )

                // ???????????????Slot
                val s = watchingCellSlot
                if (s != null) {
                    // Slot?????????
                    val baseText = """
                        Watching: (${s.x}, ${s.y})
                        ${(s.cell?.heat ?: 0.0).shorten()} HU
                        ${s.mass.shorten()} MU
                        ${s.temperature.shorten()} TU
                        ${s.heatCapacity.shorten()} HU/(MU??TU)
                        HTF=${s.heatTransferFactor.toFixed(2)}
                    """.trimIndent().split('\n').joinToString(", ")

                    listView(Direction.VERTICAL) {
                        text(baseText)

                        val c = s.cell
                        if (c == null) { // ??????Slot.cell?????????????????????Slot???cell??????
                            text("<Empty>")
                        } else { // ??????Slot.cell????????????????????????cell?????????
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
                text("????????????: ${(Game.time).shortenTime()}",
                    verticalAlign = VerticalAlign.TOP, horizontalAlign = HorizontalAlign.LEFT
                )
                text("""
                    ????????????: ${Game.factory.reactors[0].electricityCache.shorten()} EU
                    ??????: $brushStr
                    ???????????????${Game.factory.account.shorten()}
                    """.trimIndent().replace("\n", ", "),
                    verticalAlign = VerticalAlign.TOP, horizontalAlign = HorizontalAlign.LEFT
                )

                // ????????????
                listView(Direction.HORIZONTAL) {
                    val running = Game.running
                    val bgSwitchOnOff = ColorBackground(if (running) Color.GREEN else Color.RED)
                    box(50, 20, bgSwitchOnOff, onClick = { Game.running = !running }) { text(if (running) "ON" else "OFF") }
                    box(50, 20, ColorBackground(Color.YELLOW), onClick = { printData() }) { text("Print") }
                    box(50, 20, ColorBackground(Color.YELLOW), onClick = { saveData() }) { text("Save") }
                }

                var maxWidth = (Game.factory.shop.size + 2) * size

                // ??????????????????????????????????????????
                text("?????????")
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

                // ??????
                text("?????????")
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

                // ????????????Reactor????????????????????????????????????Reactor???????????????listView????????????????????????????????????????????????????????????
                listView(Direction.VERTICAL) {
                    var h = topBarHeight + size
                    for ((index, r) in Game.factory.reactors.withIndex()) {
                        box(300, 20) {
                            at(0, 10, text("Reactor #$index, status=${r.status.name}", verticalAlign = VerticalAlign.CENTER, horizontalAlign = HorizontalAlign.LEFT))
                            at(200, 0, box(100, 20, ColorBackground(if (r.running) Color.GREEN else Color.RED), onClick = { r.turn(!r.running) }) {
                                at(0, 10, text(if (r.running) "??????" else "??????", verticalAlign = VerticalAlign.CENTER, horizontalAlign = HorizontalAlign.LEFT))
                            })
                        }
                        val pixelWidth = r.width * size
                        val pixelHeight = r.height * size
                        // ????????????Reactor
                        box(pixelWidth, pixelHeight) {
                            for (slot in r.getSlots()) {
                                // ????????????Slot
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

                // ??????
                listView(Direction.VERTICAL) {
                    for ((index, item) in Game.factory.storage.withIndex()) {
                        box(300, size / 3, ColorBackground(if (storageItemUid == item.uid) Color.GRAY else Color.WHITE),
                            onClick = { storageItemUid = item.uid }) {
                            text(
                                "???${item.javaClass.simpleName}???",
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
            ???GeneratorCell???
        """.trimIndent()
        is NuclearRodCell -> """
            ???NuclearRodCell???
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
            ???NeutronMirrorCell??? 
        """.trimIndent()
        is RadiationSourceCell -> """
            ???RadiationSourceCell??? 
            radiationSpeed: ${cell.radiationSpeed.shorten()}, 
        """.trimIndent()
        is CoolingBlockCell -> "???CoolingBlockCell???"
        else -> "?????????"
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
