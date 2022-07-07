package io.theriverelder.novafactory.data.goal

import io.theriverelder.novafactory.data.NovaFactory
import io.theriverelder.novafactory.interfaces.Tickable
import io.theriverelder.novafactory.util.io.json.Persistent
import io.theriverelder.novafactory.util.io.json.ToJson

interface Level : ToJson , Persistent, Tickable {

    /**
     * 初始化该关卡，在该关卡开始时候调用，
     * 若要改动游戏设置，则使用Plugin
     */
    fun setup(factory: NovaFactory)

    /**
     * 一般在载入存档时调用
     * setup和restore总得调用一个
     */
    fun restore(factory: NovaFactory)

    /**
     * 获取当前的关卡是否成功或失败或还未定
     */
    fun getSuccessAssert(factory: NovaFactory): LevelSuccessAssert

    /**
     * 获取当前的需求，该数据会在每次tick改变，但是在同一tick内应该不变
     */
    val requirement: Double

    /**
     * 获取当前电价
     */
    val price: Double
}