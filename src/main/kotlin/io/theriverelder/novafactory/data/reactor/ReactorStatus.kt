package io.theriverelder.novafactory.data.reactor

enum class ReactorStatus(
    val isolated: Boolean,
    val running: Boolean,
) {
    // 反应堆的默认状态，单元不工作，且单元之间互相隔离
    SLEEPING(true, false),

    // 反应堆正常工作的状态
    WORKING(false, true),

    // 反应堆正在融毁，诱发原因是反应堆存在高于融毁中温度的单元。此时反应堆还会继续工作，但是会有警报
    BREAKING(false, true),

    // 反应堆已经融毁。此时反应堆的单元不工作，且都被隔离，且此反应堆永远无法被启用
    BROKEN(true, false),
    ;
}