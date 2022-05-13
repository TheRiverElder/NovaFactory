package io.theriverelder.novafactory.ui

import io.theriverelder.novafactory.ui.components.Component

typealias Sizer = Component.() -> Int

fun absSize(value: Int): Sizer = { value }

fun ratioSizeX(ratio: Int): Sizer = { width * ratio }
fun ratioSizeY(ratio: Int): Sizer = { height * ratio }

