package io.theriverelder.novafactory.util

class Creator<K, T>(
    val key: K,
    val supplier: () -> T,
) {
    fun create() = supplier()
}