package io.theriverelder.novafactory.util.math

import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.exp
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.tanh
import kotlin.random.Random

fun Double.halfLifeLossDuring(dt: Double): Double = 1 - 0.5.pow(dt / this)
fun Double.ease0to1(scale: Double = 1.0): Double = tanh(this * scale)
fun Double.sigmoid(): Double = 1 / (1 + exp(-this))

fun Double.limit(min: Double, max: Double) = this.coerceAtLeast(min).coerceAtMost(max)
fun Int.limit(min: Int, max: Int) = this.coerceAtLeast(min).coerceAtMost(max)

inline fun <reified T> Array<T>.shuffle(resultSize: Int = size): Array<T> {
    val s = resultSize.coerceAtMost(size)
    val result: Array<T> = Array(s) { this[it] }
    val r = Random(System.currentTimeMillis())
    for (i in 0 until s) {
        val index = r.nextInt(s - i)
        if (i != index) {
            val tmp = result[i]
            result[i] = result[index]
            result[index] = tmp
        }
    }
    return result
}

fun Double.toFixed(n: Int = 2): String = "%.${n}f".format(this)

private data class ShortenLevel(val maxDigits: Int, val minDigits: Int, val unit: String)
private val shortenLevels = listOf(
    ShortenLevel(3, 0, ""),
    ShortenLevel(6, 3, "k"),
    ShortenLevel(9, 6, "M"),
    ShortenLevel(12, 9, "G"),
    ShortenLevel(15, 12, "P"),
    ShortenLevel(18, 15, "T"),
)
fun Number.shorten(fixedDigits: Int = 2): String {
    var fg = fixedDigits
    val doubleValue = this.toDouble()
    val digits: Int = log10(doubleValue).toInt()
    var shortenLevel: ShortenLevel? = null
    for (sl in shortenLevels) {
        if (digits < sl.maxDigits) {
            shortenLevel = sl
            break
        }
    }
    if (shortenLevel == null) {
        shortenLevel = shortenLevels.last()
    }
    if (shortenLevel == shortenLevels.first() && isDigit()) {
        fg = 0
    }
    return (doubleValue / 10.0.pow(shortenLevel.minDigits)).toFixed(fg) + shortenLevel.unit
}

fun Number.shortenTime(): String {
    val v = this.toLong()
    val seconds = v % 60
    val minutes = (v / 60) % 60
    val hours = (v / (60 * 60)) % 24
    val days = (v / (60 * 60 * 24)) % 30
    val months = (v / (60 * 60 * 24 * 30)) % 12
    val years = (v / (60 * 60 * 24 * 30 * 12)) % 100
    return "$years years $months months $days days $hours h $minutes min $seconds s"
}

fun Number.isDigit() = (this is Long || this is Int || this is Short || this is Byte)

fun Double.isNotNaNOr(value: Double) = if (isNaN()) value else this
fun <E> initializeList(size: Int, generate: (Int) -> E) = ArrayList<E>(size).also {
    for (i in 0 until size) it.add(generate(i))
}
fun <E> LinkedList<E>.ensureLinkedListSizeAbandonHeads(size: Int) {
    while (this.size > size) {
        this.pollFirst()
    }
}