package io.theriverelder.novafactory.ui

import io.theriverelder.novafactory.ui.components.Component
import java.awt.Graphics
import javax.swing.JComponent

class Canvas(val component: Component) : JComponent() {

    override fun paint(g: Graphics?) {
        if (g != null) {
            component.render(g, Vec2(0, 0))
        }
    }
}