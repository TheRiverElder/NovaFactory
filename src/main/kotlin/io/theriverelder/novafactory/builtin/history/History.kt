package io.theriverelder.novafactory.builtin.history

import io.theriverelder.novafactory.util.math.initializeList
import kotlin.collections.ArrayList

object History {
    val reactorHistories: MutableMap<Int, DataSource> = HashMap()
    lateinit var factoryHistory: DataSource
}
