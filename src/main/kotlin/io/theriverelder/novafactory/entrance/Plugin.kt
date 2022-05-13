package io.theriverelder.novafactory.entrance

/**
 * 用于初始化一些游戏设置，只在游戏刚刚打开的时候调用
 * 若要进行关卡设置，则使用Level.setup(factory)
 */
interface Plugin {
    fun setup()
}