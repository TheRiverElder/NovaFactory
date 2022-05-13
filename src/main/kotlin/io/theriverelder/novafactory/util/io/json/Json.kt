package io.theriverelder.novafactory.util.io.json

import java.lang.Double.parseDouble
import java.lang.StringBuilder
import kotlin.math.pow

fun deserialize(input: StringReader): JsonSerializable? {
    input.skipWhitespace()
    return when (input.peek()) {
        '-' -> deserializeNumber(input)
        in '0'..'9' -> deserializeNumber(input)
        '"' -> deserializeString(input)
        't', 'f' -> deserializeBoolean(input)
        'n', 'u' -> deserializeNullOrUndefined(input)
        '[' -> deserializeArray(input)
        '{' -> deserializeObject(input)
        else -> null
    }
}

fun deserializeBoolean(input: StringReader): JsonBoolean? {
    input.skipWhitespace()
    val value = when {
        input.read("true") -> true
        input.read("false") -> false
        else -> return null
    }
    return JsonBoolean(value)
}

fun deserializeNullOrUndefined(input: StringReader): JsonNullOrUndefined? {
    input.skipWhitespace()
    val isNull = when {
        input.read("null") -> true
        input.read("undefined") -> false
        else -> return null
    }
    return JsonNullOrUndefined(isNull)
}

val NUMBER_REGEX = Regex("[+-]?\\d+(\\d*\\.\\d+)?([eE][+-]?\\d+)?")
fun deserializeNumber(input: StringReader): JsonNumber? {
    val r = input.read(NUMBER_REGEX) ?: return null
    return try {
        JsonNumber(parseDouble(r))
    } catch (e: Exception) {
        null
    }
}

fun deserializeNumber1(input: StringReader): JsonNumber? {
    input.skipWhitespace()
    var start = input.next

    var status = ParsingStatus.INT_PART

    var intPart: Long = 0L
    var decimalPart: Double = 0.0
    var decimalWeightExp: Int = 0
    var exponentPart: Int = 0

    while (input.hasMore) {
        val ch = input.peek()
        when (status) {
            ParsingStatus.INT_PART -> {
                if (ch.isDigit()) {
                    intPart *= 10
                    intPart += ch - '0'
                    input.read()
                } else {
                    if (start >= input.next) return null
                    start = input.next
                    status = ParsingStatus.DOT
                }
            }
            ParsingStatus.DOT -> {
                status =
                    if (input.read(".")) ParsingStatus.DECIMAL_PART
                    else ParsingStatus.E
            }
            ParsingStatus.DECIMAL_PART -> {
                if (ch.isDigit()) {
                    decimalWeightExp--
                    decimalPart += (ch - '0') * 10.0.pow(decimalWeightExp)
                    input.read()
                } else {
                    start = input.next
                    status = ParsingStatus.E
                }
            }
            ParsingStatus.E -> {
                status =
                    if (input.read("e") || input.read("E")) ParsingStatus.EXPONENT_PART
                    else ParsingStatus.EXIT
            }
            ParsingStatus.EXPONENT_PART -> {
                if (ch.isDigit()) {
                    exponentPart *= 10
                    exponentPart += ch - '0'
                    input.read()
                } else {
                    start = input.next
                    status = ParsingStatus.EXIT
                }
            }
            ParsingStatus.EXIT -> break
        }
    }

    input.next = start

    val finalValue: Number = if (decimalWeightExp == 0 && exponentPart == 0) {
        if (intPart in Int.MIN_VALUE..Int.MAX_VALUE) intPart.toInt() else intPart
    } else {
        (intPart + decimalPart)* 10.0.pow(exponentPart)
    }

    return JsonNumber(finalValue)
}


enum class ParsingStatus {
    INT_PART,
    DOT,
    DECIMAL_PART,
    E,
    EXPONENT_PART,
    EXIT,
}

fun deserializeString(input: StringReader): JsonString? {
    input.skipWhitespace()
    val value = readString(input) ?: return null
    return JsonString(value)
}

fun readString(input: StringReader): String? {
    val start = input.next
    var escaped: Boolean = false
    val builder = StringBuilder()

    if (!input.read("\"")) return null

    var fine = false
    while (input.hasMore) {
        val ch = input.read()
        if (escaped) {
            val actualChar = when (ch) {
                't' -> '\t'
                'n' -> '\n'
                'r' -> '\r'
                else -> ch
            }
            builder.append(actualChar)
            escaped = false
            continue
        }
        when (ch) {
            '\\' -> escaped = true
            '"' -> {
                fine = true
                break
            }
            else -> builder.append(ch)
        }
    }

    if (!fine) {
        input.next = start
        return null
    }

    return builder.toString()
}

fun deserializeArray(input: StringReader): JsonArray? {
    input.skipWhitespace()
    val start = input.next

    if (!input.read("[")) return null
    input.skipWhitespace()

    val list = ArrayList<JsonSerializable>()

    var fine = false
    while (input.hasMore) {
        if (input.read("]")) {
            fine = true
            break
        }
        val elem = deserialize(input) ?: break
        list.add(elem)
        input.skipWhitespace()
        if (input.read(",")) {
            input.skipWhitespace()
        }
    }

    if (!fine) {
        input.next = start
        return null
    }

    return JsonArray(*list.toTypedArray())
}

fun deserializeObject(input: StringReader): JsonObject? {
    input.skipWhitespace()
    val start = input.next

    if (!input.read("{")) return null
    input.skipWhitespace()

    val list = ArrayList<Pair<String, JsonSerializable>>()

    var fine = false
    while (input.hasMore) {
        if (input.read("}")) {
            fine = true
            break
        }
        val key = readString(input) ?: return null

        input.skipWhitespace()
        if (!input.read(":")) return null
        input.skipWhitespace()

        val value = deserialize(input) ?: break

        list.add(Pair(key, value))
        input.skipWhitespace()
        if (input.read(",")) {
            input.skipWhitespace()
        }
    }

    if (!fine) {
        input.next = start
        return null
    }

    return JsonObject(*list.toTypedArray())
}

operator fun String.unaryPlus() = JsonString(this)
operator fun Number.unaryPlus() = JsonNumber(this)
operator fun Boolean.unaryPlus() = JsonBoolean(this)

fun JsonSerializable.convertToString(): String {
    val output: StringBuilder = StringBuilder()
    this.serialize(output)
    return output.toString()
}