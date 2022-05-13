package io.theriverelder.novafactory.util

import java.util.*

fun String.explode(charCount: Int): List<String> {
    var i = 0
    val r = LinkedList<String>()
    while (i < this.length) {
        r.add(substring(i, (i + charCount).coerceAtMost(this.length)))
        i += charCount
    }
    return r
}

fun String.escape(): String = this.replace("\"", "\\\"")