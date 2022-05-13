package io.theriverelder.novafactory.ui.components

import io.theriverelder.novafactory.builtin.history.DataSource
import io.theriverelder.novafactory.ui.Vec2
import io.theriverelder.novafactory.ui.MouseEventType
import io.theriverelder.novafactory.util.math.shorten
import java.awt.*
import java.lang.Integer.max as maxInt
import java.lang.Double.max
import java.util.*

class LineChart(
    override val width: Int,
    override val height: Int,
    val padding: Int,
    val minScale: Double,
    val dataSource: DataSource
) : NonRootComponent() {

    override var x: Int = 0
        private set
    override var y: Int = 0
        private set

    override fun render(g: Graphics, pos: Vec2) {
        x = pos.x
        y = pos.y

        if (g is Graphics2D) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        g.color = Color.WHITE
        g.fillRect(pos.x, pos.y, width, height)
        g.color = Color.BLACK

        val data = dataSource.getSlice()

        val keyList = data.map { it.xLabel }
        val valuesList = data.map { it.values }
        val maxValue = max(valuesList.flatten().maxOrNull() ?: minScale, minScale) * 1.2
        val minValue = 0.0

        val fontMetrics = g.fontMetrics

        val maxValueString = maxValue.shorten()
        val minValueString = minValue.shorten()
        val yLabelWidth = maxInt(
            fontMetrics.stringWidth("$maxValueString "),
            fontMetrics.stringWidth("$minValueString ")
        )
        val xLabelHeight = fontMetrics.height

        val mainAreaX = pos.x + padding + yLabelWidth
        val mainAreaY = pos.y + padding
        val mainAreaWidth = width - 2 * padding - yLabelWidth
        val mainAreaHeight = height - 2 * padding - xLabelHeight
        val step = mainAreaWidth / maxInt(valuesList.size, 1)

        var valueIndex = 0
        while (true) {
            val values = valuesList.map { it.getOrNull(valueIndex) }
            if (values.isEmpty() || values.all { it == null }) break

            var prevPixelX: Int = mainAreaX
            var prevPixelY: Int = mainAreaY +
                    (mainAreaHeight * (1 - (values.first { it is Double } as Double) / maxValue)).toInt()

            // 绘制内容
            for ((index, value) in values.withIndex()) {
                if (value == null) continue

                val pixelX: Int = mainAreaX + ((index + 0.5) * step).toInt()
                val pixelY: Int = mainAreaY + (mainAreaHeight * (1 - value / maxValue)).toInt()

                val valueString = value.shorten()
                val valueStringWidth = fontMetrics.stringWidth(valueString)

                // 绘制该点到X轴的铅垂线
                g.color = Color.LIGHT_GRAY
                g.drawLine(pixelX, pixelY, pixelX, mainAreaY + mainAreaHeight)

                // 绘制数值文字
                g.color = Color.BLUE
                g.drawString(valueString, pixelX - valueStringWidth / 2, pixelY - fontMetrics.descent)

                // 绘制折线
                g.color = Color.BLACK
                g.drawLine(prevPixelX, prevPixelY, pixelX, pixelY)

                prevPixelX = pixelX
                prevPixelY = pixelY
            }

            valueIndex++
        }

        // 绘制横坐标标签
        g.color = Color.BLACK
        g.drawLine(mainAreaX, mainAreaY, mainAreaX, mainAreaY + mainAreaHeight)
        g.drawLine(mainAreaX, mainAreaY + mainAreaHeight, mainAreaX + mainAreaWidth, mainAreaY + mainAreaHeight)

        g.drawString(maxValueString, mainAreaX - yLabelWidth, mainAreaY)
        g.drawString(minValueString, mainAreaX - yLabelWidth, mainAreaY + mainAreaHeight)

        val xLabelY = mainAreaY + mainAreaHeight + xLabelHeight
        for ((index, key) in keyList.withIndex()) {
            val pixelX: Int = mainAreaX + ((index + 0.5) * step).toInt()
            val xLabel = key.shorten()
            val xLabelWidth = fontMetrics.stringWidth(xLabel)
            g.drawString(xLabel, pixelX - xLabelWidth / 2, xLabelY)
        }
    }

    override fun mouse(type: MouseEventType, mousePosition: Vec2) {
//        TODO("Not yet implemented")
    }

    override fun getIndependentSize(fontMetrics: FontMetrics): Vec2 {
        return Vec2(width, height)
    }

}

fun ContainerComponent.lineChart(
    width: Int,
    height: Int,
    padding: Int = 10,
    minScale: Double = 1.0,
    data: DataSource
): LineChart {
    val lc = LineChart(width, height, padding, minScale, data)
    this.add(lc)
    return lc
}