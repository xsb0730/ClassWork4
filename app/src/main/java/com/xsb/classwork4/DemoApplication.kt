package com.xsb.classwork4

import android.app.Application
import com.xsb.sdk.core.MonitorConfig
import com.xsb.sdk.core.PerformanceMonitor

class DemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 初始化SDK
        val config = MonitorConfig(
            enableFluencyMonitor = true,
            fluencyThresholdMs = 16,
            fluencyReportInterval = 5000,
            enableANRMonitor = true,
            anrThresholdMs = 5000,
            anrCheckInterval = 1000,
            debugMode = true
        )

        PerformanceMonitor.init(this, config)
        PerformanceMonitor.start()
    }
}