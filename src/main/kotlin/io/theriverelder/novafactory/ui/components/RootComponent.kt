package io.theriverelder.novafactory.ui.components

import io.theriverelder.novafactory.ui.Canvas
import io.theriverelder.novafactory.ui.Vec2
import io.theriverelder.novafactory.ui.MouseEventType
import java.awt.BorderLayout
import java.awt.FontMetrics
import java.awt.Graphics
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.*
import javax.swing.JFrame

class RootComponent(val init: Initiator<RootComponent>) : ContainerComponent {

    val frame: JFrame = JFrame()
    val canvas: Canvas = Canvas(this)

    init {
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.isResizable = false
        canvas.setBounds(0, 0, frame.width, frame.height)
        frame.add(canvas, BorderLayout.CENTER)

        canvas.addMouseListener(RootComponentMouseListener(this))
    }

    var title: String
        get() = frame.title
        set(value) {
            frame.title = value
        }

    override var x: Int
        get() = frame.x
        set(value) = frame.setLocation(value, y)

    override var y: Int
        get() = frame.x
        set(value) = frame.setLocation(x, value)

    override var width: Int
        get() = frame.x
        set(value) {
            frame.setSize(value, height)
            canvas.setSize(value, height)
        }

    override var height: Int
        get() = frame.x
        set(value) {
            frame.setSize(width, value)
            canvas.setSize(width, value)
        }

    var visible: Boolean
        get() = frame.isVisible
        set(value) { frame.isVisible = value }

    fun show() {
        visible = true
        render()
    }

    private val children: MutableList<Component> = LinkedList()

    override fun add(component: NonRootComponent) {
        children += component
        component.parent = this
    }

    override fun render(g: Graphics, pos: Vec2) {
        for (child in children) {
            child.render(g, pos)
        }
    }

    fun render() {
        children.clear()
        init()
        canvas.repaint()
    }

    override fun mouse(type: MouseEventType, mousePosition: Vec2) {
        for (child in children) {
            child.mouse(type, mousePosition)
        }
    }

    override fun getIndependentSize(fontMetrics: FontMetrics): Vec2 {
//        TODO("Not yet implemented")
        return Vec2.ZERO
    }

    fun setLocation(x: Int, y: Int) = frame.setLocation(x, y)
    fun setSize(width: Int, height: Int) = frame.setSize(width, height)
    fun setBounds(x: Int, y: Int, width: Int, height: Int) = frame.setBounds(x, y, width, height)
}

class RootComponentMouseListener(val root: RootComponent) : MouseAdapter() {
    override fun mouseClicked(e: MouseEvent?) {
        if (e != null) {
            root.mouse(MouseEventType.CLICK, Vec2(e.x, e.y))
        }
    }

    override fun mouseMoved(e: MouseEvent?) {
        if (e != null) {
            root.mouse(MouseEventType.HOVER, Vec2(e.x, e.y))
        }
    }
}