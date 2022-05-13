package io.theriverelder.novafactory.ui.components

import io.theriverelder.novafactory.ui.Vec2
import io.theriverelder.novafactory.ui.MouseEventType
import java.awt.Color
import java.awt.FontMetrics
import java.awt.Graphics
import kotlin.math.max

enum class HorizontalAlign {
    LEFT,
    CENTER,
    RIGHT,
}

enum class VerticalAlign {
    TOP,
    CENTER,
    BOTTOM,
}

class Text(
    val content: String = "",
    val color: Color,
    val verticalAlign: VerticalAlign,
    val horizontalAlign: HorizontalAlign,
    val lineHeight: Int,
) : NonRootComponent() {

    private val lines: List<String> = content.split(Regex("\r?\n"))

    override var x: Int = 0
        private set

    override var y: Int = 0
        private set

    override var width: Int = 0
        private set

    override var height: Int = 0
        private set

    override fun render(g: Graphics, pos: Vec2) {
        x = pos.x
        y = pos.y
        width = 0
        height = 0

        if (lines.isEmpty() || lines[0].isBlank()) return

        val fm = g.fontMetrics
        val ascent = fm.ascent
//        val descent = fm.descent

        val lineHeight = if (this.lineHeight >= 0) this.lineHeight else (fm.height)
        height = lineHeight * lines.size

        g.color = color

        val totalDy = when (verticalAlign) {
            VerticalAlign.TOP -> 0
            VerticalAlign.CENTER -> -height / 2
            VerticalAlign.BOTTOM -> -height
        }

        for ((i, line) in lines.withIndex()) {
            val lineWidth = fm.stringWidth(line)
            width = max(width, lineWidth)

            val dx = when (horizontalAlign) {
                HorizontalAlign.LEFT -> 0
                HorizontalAlign.CENTER -> -lineWidth / 2
                HorizontalAlign.RIGHT -> -lineWidth
            }

            val dy = totalDy + i * lineHeight + ascent

            g.drawString(line, pos.x + dx, pos.y + dy)
        }
    }

    override fun mouse(type: MouseEventType, mousePosition: Vec2) {
//        TODO("Not yet implemented")
    }

    override fun getIndependentSize(fontMetrics: FontMetrics): Vec2 {
        val lineHeight = if (this.lineHeight >= 0) this.lineHeight else (fontMetrics.height)
        val height = lineHeight * lines.size
        val width: Int = lines.maxOfOrNull(fontMetrics::stringWidth) ?: 0
        return Vec2(width, height)
    }
}

fun ContainerComponent.text(
    content: String = "",
    color: Color = Color.BLACK,
    verticalAlign: VerticalAlign = VerticalAlign.TOP,
    horizontalAlign: HorizontalAlign = HorizontalAlign.LEFT,
    lineHeight: Int = -1,
): Text {
    val t = Text(content, color, verticalAlign, horizontalAlign, lineHeight)
    add(t)
    return t
}