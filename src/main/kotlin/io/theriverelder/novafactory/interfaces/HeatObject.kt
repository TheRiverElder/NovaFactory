package io.theriverelder.novafactory.interfaces

interface HeatObject {
    // 热量，单位：H
    var heat: Double
    // 热量传输因子，单位：1/(H/M)，范围在[0, 1]，意为在该温度下，有heatTransferFactor * temperature的热量会被散发，吸收的热只占全部的heatTransferFactor
    val heatTransferFactor: Double
    // 比热容，单位：H/(M·(H/M))，意味在该温度下，要使单位质量升高单位温度所需的热量
    val heatCapacity: Double
}