package com.xsb.sdk.core

import android.app.Application
import com.xsb.sdk.anr.ANRMonitor
import com.xsb.sdk.fluency.FluencyMonitor
import com.xsb.sdk.reporter.IReporter
import com.xsb.sdk.reporter.LogReporter

//SDK单例入口
object PerformanceMonitor {

    private lateinit var application: Application
    private lateinit var config: MonitorConfig
    private lateinit var reporter: IReporter

    private var fluencyMonitor: FluencyMonitor? = null
    private var anrMonitor: ANRMonitor? = null

    private var isInitialized = false

    /**
     * 初始化SDK
     */
    fun init(
        app: Application,
        config: MonitorConfig = MonitorConfig(),
        reporter: IReporter = LogReporter()
    ) {
        if (isInitialized) {
            android.util.Log.w("PerformanceMonitor", "SDK已初始化，忽略重复调用")
            return
        }

        this.application = app
        this.config = config
        this.reporter = reporter

        // 初始化监控器
        if (config.enableFluencyMonitor) {
            fluencyMonitor = FluencyMonitor(config, reporter)
        }

        if (config.enableANRMonitor) {
            anrMonitor = ANRMonitor(config, reporter)
        }

        isInitialized = true

        if (config.debugMode) {
            android.util.Log.d("PerformanceMonitor", "SDK初始化完成")
        }
    }

    /**
     * 启动监控
     */
    fun start() {
        checkInitialized()

        fluencyMonitor?.start()
        anrMonitor?.start()

        if (config.debugMode) {
            android.util.Log.d("PerformanceMonitor", "监控已启动")
        }
    }

    /**
     * 停止监控
     */
    fun stop() {
        checkInitialized()

        fluencyMonitor?.stop()
        anrMonitor?.stop()

        if (config.debugMode) {
            android.util.Log.d("PerformanceMonitor", "监控已停止")
        }
    }

    /**
     * 自定义上报器
     */
    fun setReporter(reporter: IReporter) {
        this.reporter = reporter
    }

    private fun checkInitialized() {
        check(isInitialized) { "PerformanceMonitor未初始化，请先调用init()" }
    }
}