package com.xsb.sdk.model

//流畅性数据模型
data class FluencyData(
    val timestamp: Long,
    val avgFps: Float,
    val jankyFrameCount: Int,
    val maxFrameTimeMs: Long,
    val pageName: String
)