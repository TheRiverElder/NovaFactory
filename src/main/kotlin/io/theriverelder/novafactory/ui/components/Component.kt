package io.theriverelder.novafactory.ui.components

import io.theriverelder.novafactory.ui.Vec2
import io.theriverelder.novafactory.ui.MouseEventType
import java.awt.FontMetrics
import java.awt.Graphics

typealias DimGetter = (Component.() -> Vec2)

interface Component {

    // 以下4个属性只是缓存一下上次渲染后的状态
    val x: Int
    val y: Int
    val width: Int
    val height: Int

//    val position: DimGetter?
//    val size: DimGetter?

    fun render(g: Graphics, pos: Vec2)

    fun mouse(type: MouseEventType, mousePosition: Vec2)

    fun getIndependentSize(fontMetrics: FontMetrics): Vec2

}