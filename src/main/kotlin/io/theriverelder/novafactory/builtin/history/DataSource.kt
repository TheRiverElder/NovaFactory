package io.theriverelder.novafactory.builtin.history

import io.theriverelder.novafactory.util.io.json.*
import io.theriverelder.novafactory.util.math.ensureLinkedListSizeAbandonHeads
import java.util.*

class DataSource(initialRecords: List<DataRecord>) {
    val records: LinkedList<DataRecord> = LinkedList(initialRecords)
    private var windowStart: Int = 0 // inclusive
    private var windowEnd: Int = 0 // exclusive

    fun addRecord(record: DataRecord) {
        records.add(record)
        records.ensureLinkedListSizeAbandonHeads(windowEnd - windowStart)
    }

    fun setWindow(start: Int, end: Int) {
        windowStart = start
        windowEnd = end
    }

    fun setWindowToLast(size: Int) {
        setWindow(-size, 0)
    }

    fun getSlice(): List<DataRecord> {
        val s = records.size
        val start = (windowStart + s) % s
        val end = if (windowStart < 0 && windowEnd == 0) s else (windowEnd + s) % s
        return records.slice(start until end)
    }
}

data class DataRecord(val xLabel: Long, val values: List<Double>) : ToJson {
    override fun toJson(): JsonSerializable {
        return JsonArray(listOf(xLabel, *values.toTypedArray()).map { JsonNumber(it) } )
    }

}
data class DataPoint(val xLabel: Long, val value: Double)