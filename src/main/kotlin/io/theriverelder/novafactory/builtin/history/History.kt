package io.theriverelder.novafactory.builtin.history

object History {
    val reactorHistories: MutableMap<Int, DataSource> = HashMap()
    lateinit var factoryHistory: DataSource
}
