package io.theriverelder.novafactory.ui.background

import io.theriverelder.novafactory.ui.components.Component
import io.theriverelder.novafactory.ui.Vec2
import java.awt.Graphics

interface Background {
    fun render(g: Graphics, component: Component, offset: Vec2)
}