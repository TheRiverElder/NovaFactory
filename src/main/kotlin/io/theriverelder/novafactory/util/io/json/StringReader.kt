package io.theriverelder.novafactory.util.io.json

class StringReader(val str: String) {

    var next: Int = 0

    val hasMore: Boolean
        get() = next < str.length

    fun read(): Char = str[next++]

    fun peek(): Char = str[next]

    fun read(expect: String): Boolean {
        val r = str.length >= next + expect.length && str.substring(next, next + expect.length) == expect
        if (r) {
            next += expect.length
        }
        return r
    }

    fun read(predicate: Char.() -> Boolean): String {
        val start = next
        while (hasMore && peek().predicate()) {
            read()
        }
        return str.substring(start, next)
    }

    fun read(regex: Regex): String? {
        val result = regex.find(str, next) ?: return null
        if (result.range.first != next) return null
        val s = result.value
        next += s.length
        return s
    }

    fun skipWhitespace() {
        while (hasMore && peek().isWhitespace()) {
            read()
        }
    }
}