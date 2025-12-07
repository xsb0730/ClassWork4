package com.xsb.sdk.core

//配置数据类
data class MonitorConfig(
    // 流畅性监控配置
    val enableFluencyMonitor: Boolean = true,
    val fluencyThresholdMs: Long = 16,      // 帧率阈值(16ms = 60fps)
    val fluencyReportInterval: Long = 5000, // 上报间隔

    // ANR监控配置
    val enableANRMonitor: Boolean = true,
    val anrThresholdMs: Long = 5000,        // ANR阈值
    val anrCheckInterval: Long = 1000,      // 检查间隔

    // 通用配置
    val debugMode: Boolean = false
)