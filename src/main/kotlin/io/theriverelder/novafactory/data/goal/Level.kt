package io.theriverelder.novafactory.data.goal

import io.theriverelder.novafactory.data.NovaFactory
import io.theriverelder.novafactory.util.io.json.ToJson

interface Level : ToJson {

    /**
     * 初始化该关卡，在该关卡开始时候调用，
     * 若要改动游戏设置，则使用Plugin
     */
    fun setup(factory: NovaFactory)

    /**
     * 获取当前的关卡是否成功或失败或还未定
     */
    fun getSuccessAssert(factory: NovaFactory): LevelSuccessAssert

    fun getInfo(factory: NovaFactory): String

    fun input(factory: NovaFactory, double: Double)
}