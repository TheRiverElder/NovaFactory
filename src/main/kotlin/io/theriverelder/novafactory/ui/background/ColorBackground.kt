package io.theriverelder.novafactory.ui.background

import io.theriverelder.novafactory.ui.components.Component
import io.theriverelder.novafactory.ui.Vec2
import java.awt.Color
import java.awt.Graphics

class ColorBackground(val color: Color) : Background {

    override fun render(g: Graphics, component: Component, offset: Vec2) {
        g.color = color
        g.fillRect(offset.x, offset.y, component.width, component.height)
    }
}