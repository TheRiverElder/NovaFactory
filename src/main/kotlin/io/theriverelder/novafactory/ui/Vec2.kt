package io.theriverelder.novafactory.ui

class Vec2(val x: Int, val y: Int) {

    companion object {
        val ZERO: Vec2 = Vec2(0, 0)
    }

    operator fun plus(vec2: Vec2) = Vec2(x + vec2.x, y + vec2.y)
    operator fun minus(vec2: Vec2) = Vec2(x - vec2.x, y - vec2.y)

    fun offset(dx: Int, dy: Int) = Vec2(x + dx, y + dy)

    override fun toString(): String {
        return "($x, $y)"
    }
}