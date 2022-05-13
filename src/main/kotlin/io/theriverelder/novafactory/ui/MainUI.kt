package io.theriverelder.novafactory.ui

import io.theriverelder.novafactory.ui.components.ContainerInitiator
import io.theriverelder.novafactory.ui.components.Initiator
import io.theriverelder.novafactory.ui.components.RootComponent
import javax.swing.JFrame

fun createUI(
    x: Int = 0,
    y: Int = 0,
    width: Int = 0,
    height: Int = 0,
    title: String = "Nova Factory",
    init: ContainerInitiator
): RootComponent {
    val root = RootComponent(init)

    root.setBounds(x, y, width, height)
    root.title = title

    root.init()

    val frame = root.frame
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

    return root
}