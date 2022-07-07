package io.theriverelder.novafactory.util.io.json

// 保存和读取存档时使用
interface Persistent {
    fun read(json: JsonObject)
    fun write(): JsonObject
}