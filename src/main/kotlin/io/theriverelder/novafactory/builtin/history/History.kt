package io.theriverelder.novafactory.builtin.history

import io.theriverelder.novafactory.util.math.initializeList
import kotlin.collections.ArrayList

object History {
    val reactorHistories: MutableList<DataSource> = ArrayList()
    val factoryHistory: DataSource = DataSource(initializeList(10) { DataRecord(0, listOf(0.0)) })
}
