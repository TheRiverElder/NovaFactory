package io.theriverelder.novafactory.util.math

import java.awt.Color
import kotlin.math.floor
import kotlin.math.tanh

fun Double.toHeatColor(upLimit: Double = Double.POSITIVE_INFINITY): Color {
    val v = ((this / upLimit).clamp(0.0, 1.0) * 0xFF).toInt()
    return Color(v, 0, 0xFF - v, 0xFF)
}

fun Double.toHeatColorInfinity(scalar: Double = 1.0): Color {
    val v = floor(tanh(this * scalar) * 0x0100).toInt().clamp(0, 0xFF)
    return Color(v, 0, 0xFF - v, 0xFF)
}

//fun Double.toHeatColor(): Color {
//    val v = (this.limit(0.0, 1.0) * 0x0300).toInt()
//    val p = v % 0x0100
//
//    var r = 0
//    var g = 0
//    var b = 0
//
//    when {
//        v < 0x0100 -> {
//            b = p
//        }
//        v < 0x0200 -> {
//            g = p
//            b = 0xFF - p
//        }
//        else -> {
//            r = p
//            g = 0xFF - p
//        }
//    }
//    return Color(r, g, b, 0xFF)
//}