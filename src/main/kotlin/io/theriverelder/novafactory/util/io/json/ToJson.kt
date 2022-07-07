package io.theriverelder.novafactory.util.io.json

// 只是作为和客户端的交互展示信息使用
interface ToJson {

    fun toJson(): JsonSerializable

}