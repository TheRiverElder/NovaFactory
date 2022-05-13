package io.theriverelder.novafactory

import io.theriverelder.novafactory.ui.*
import io.theriverelder.novafactory.ui.background.ColorBackground
import io.theriverelder.novafactory.ui.components.HorizontalAlign
import io.theriverelder.novafactory.ui.components.VerticalAlign
import io.theriverelder.novafactory.ui.components.box
import io.theriverelder.novafactory.ui.components.text
import java.awt.Color
import java.awt.Toolkit

fun main() {
    val width = 800
    val height = 600
    val screenSize = Toolkit.getDefaultToolkit().screenSize

    val root = createUI(
        x = screenSize.width / 2 - width / 2,
        y = screenSize.height / 2 - height / 2,
        width = width,
        height = height,
        "Hello NovaFactory"
    ) {
        box(width = 200, height = 100, background = ColorBackground(Color.CYAN)) {
            box(width = 50, height = 14, background = ColorBackground(Color.RED))
            text("Hello World!",
                verticalAlign = VerticalAlign.TOP,
                horizontalAlign = HorizontalAlign.LEFT
            )
        }
    }

    root.show()
}