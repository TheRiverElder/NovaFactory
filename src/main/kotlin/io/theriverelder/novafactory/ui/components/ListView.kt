package io.theriverelder.novafactory.ui.components

import io.theriverelder.novafactory.ui.MouseEventType
import io.theriverelder.novafactory.ui.Vec2
import java.awt.FontMetrics
import java.awt.Graphics
import java.util.*
import kotlin.math.max

enum class Direction {
//    LEFT_TO_RIGHT,
//    RIGHT_TO_LEFT,
//    TOP_TO_BOTTOM,
//    BOTTOM_TO_TOP,
    VERTICAL,
    HORIZONTAL,
}

class ListView(
    val direction: Direction,
) : ContainerComponent, NonRootComponent() {

    override var x: Int = 0
        private set

    override var y: Int = 0
        private set

    override var width: Int = 0
        private set

    override var height: Int = 0
        private set

    private val children: MutableList<Component> = LinkedList()

    override fun add(component: NonRootComponent) {
        children += component
        component.parent = this
    }

    override fun render(g: Graphics, pos: Vec2) {
        x = pos.x
        y = pos.y
        width = 0
        height = 0
        if (direction == Direction.VERTICAL) {
            val x = 0
            var y = 0
            for (child in children) {
                val size = child.getIndependentSize(g.fontMetrics)
                width = max(width, size.x)
                height += size.y
                val p = pos + Vec2(x, y)
                child.render(g, p)
                y += size.y
            }
        } else {
            var x = 0
            val y = 0
            for (child in children) {
                val size = child.getIndependentSize(g.fontMetrics)
                width += size.x
                height = max(height, size.y)
                val p = pos + Vec2(x, y)
                child.render(g, p)
                x += size.x
            }
        }
    }

    override fun mouse(type: MouseEventType, mousePosition: Vec2) {
        children.forEach { it.mouse(type, mousePosition) }
    }

    override fun getIndependentSize(fontMetrics: FontMetrics): Vec2 {
        var width = 0
        var height = 0
        if (direction == Direction.VERTICAL) {
            for (child in children) {
                val size = child.getIndependentSize(fontMetrics)
                width = max(width, size.x)
                height += size.y
            }
        } else {
            for (child in children) {
                val size = child.getIndependentSize(fontMetrics)
                width += size.x
                height = max(height, size.y)
            }
        }
        return Vec2(width, height)
    }
}

fun ContainerComponent.listView(
    direction: Direction = Direction.VERTICAL,
    init: ContainerInitiator = {},
): ListView {
    val v = ListView(direction)
    v.init()
    this.add(v)
    return v
}