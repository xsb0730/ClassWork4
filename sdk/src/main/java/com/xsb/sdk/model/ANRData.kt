package com.xsb.sdk.model

//ANR数据模型
data class ANRData(
    val timestamp: Long,
    val blockTimeMs: Long,
    val pageName: String,
    val mainThreadStackTrace: String
)