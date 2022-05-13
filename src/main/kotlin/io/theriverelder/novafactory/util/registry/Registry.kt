package io.theriverelder.novafactory.util.registry

class Registry<K, V>(val getKey: (V) -> K) {

    private val map = HashMap<K, V>()

    fun register(value: V) {
        val key: K = getKey(value)
        this[key] = value
    }

    operator fun set(key: K, value: V) { map[key] = value }

    operator fun get(key: K): V = map[key] ?: throw Exception("$key does not exist")

    fun tryGet(key: K): V? = map[key]

    fun getAll(): List<V> = map.values.toList()
}