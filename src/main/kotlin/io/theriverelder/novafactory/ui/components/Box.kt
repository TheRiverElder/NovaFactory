package io.theriverelder.novafactory.ui.components

import io.theriverelder.novafactory.ui.Vec2
import io.theriverelder.novafactory.ui.MouseEventType
import io.theriverelder.novafactory.ui.background.Background
import io.theriverelder.novafactory.ui.background.ColorBackground
import java.awt.Color
import java.awt.FontMetrics
import java.awt.Graphics
import java.util.*
import kotlin.collections.HashMap

class Box(
    override val width: Int,
    override val height: Int,
    val background: Background,
    val borderColor: Color?,
    val onClick: Box.(Vec2) -> Unit,
    val onHover: Box.(Vec2) -> Unit,
) : NonRootComponent(), ContainerComponent {

    override var x: Int = 0
        private set
    override var y: Int = 0
        private set

    private val children: MutableList<Component> = LinkedList()

    override fun add(component: NonRootComponent) {
        children += component
        component.parent = this
    }

    override fun render(g: Graphics, pos: Vec2) {
        x = pos.x
        y = pos.y

        background.render(g, this, pos)

        for (child in children) {
            child.render(g, pos + componentPositions.getOrDefault(child, Vec2.ZERO))
        }

        val left = x + 3
        val right = x + width - 3
        val top = y + 3
        val bottom = y + height - 3

        if (borderColor != null) {
            g.color = borderColor
            g.drawPolyline(
                arrayOf(left, right, right, left, left).toIntArray(),
                arrayOf(top, top, bottom, bottom, top).toIntArray(),
                5
            )
        }
    }

    override fun mouse(type: MouseEventType, mousePosition: Vec2) {
        for (child in children) {
            child.mouse(type, mousePosition)
        }

        val left = x
        val right = x + width
        val top = y
        val bottom = y + height

        if (mousePosition.x >= left && mousePosition.y >= top && mousePosition.x < right && mousePosition.y < bottom) {
            if (type == MouseEventType.CLICK) {
                onClick(mousePosition)
            } else if (type == MouseEventType.HOVER) {
                onHover(mousePosition)
            }
        }
    }

    override fun getIndependentSize(fontMetrics: FontMetrics): Vec2 {
        return Vec2(width, height)
    }

    private val componentPositions: MutableMap<Component, Vec2> = HashMap()

    fun setPosition(component: Component, x: Int, y: Int) {
        componentPositions[component] = Vec2(x, y)
    }
}

fun ContainerComponent.box(
    width: Int,
    height: Int,
    background: Background = ColorBackground(Color.WHITE),
    borderColor: Color? = null,
    onClick: (Box.(Vec2) -> Unit)? = null,
    onHover: (Box.(Vec2) -> Unit)? = null,
    init: Initiator<Box> = {},
): Box {
    val b = Box(width, height, background, borderColor, onClick ?: {}, onHover ?: {})
    b.init()
    add(b)
    return b
}

fun Box.at(
    x: Int,
    y: Int,
    component: Component,
) {
    setPosition(component, x, y)
}