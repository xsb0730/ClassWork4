package com.xsb.sdk.fluency

import android.os.Handler
import android.os.Looper
import android.view.Choreographer
import com.xsb.sdk.core.MonitorConfig
import com.xsb.sdk.model.FluencyData
import com.xsb.sdk.reporter.IReporter
import java.util.concurrent.ConcurrentLinkedQueue

//流畅性监控核心
class FluencyMonitor(
    private val config: MonitorConfig,
    private val reporter: IReporter
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isMonitoring = false

    private val frameTimeQueue = ConcurrentLinkedQueue<Long>()
    private var lastFrameTimeNanos = 0L
    private var jankyFrameCount = 0
    private var maxFrameTime = 0L

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!isMonitoring) return

            if (lastFrameTimeNanos != 0L) {
                val frameTimeMs = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000
                frameTimeQueue.offer(frameTimeMs)

                // 判断卡顿
                if (frameTimeMs > config.fluencyThresholdMs) {
                    jankyFrameCount++
                }

                // 记录最大帧耗时
                if (frameTimeMs > maxFrameTime) {
                    maxFrameTime = frameTimeMs
                }
            }

            lastFrameTimeNanos = frameTimeNanos
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    private val reportRunnable = object : Runnable {
        override fun run() {
            if (!isMonitoring) return

            reportMetrics()
            mainHandler.postDelayed(this, config.fluencyReportInterval)
        }
    }

    fun start() {
        if (isMonitoring) return

        isMonitoring = true
        lastFrameTimeNanos = 0L
        jankyFrameCount = 0
        maxFrameTime = 0
        frameTimeQueue.clear()

        Choreographer.getInstance().postFrameCallback(frameCallback)
        mainHandler.postDelayed(reportRunnable, config.fluencyReportInterval)

        if (config.debugMode) {
            android.util.Log.d("FluencyMonitor", "流畅性监控已启动")
        }
    }

    fun stop() {
        if (!isMonitoring) return

        isMonitoring = false
        Choreographer.getInstance().removeFrameCallback(frameCallback)
        mainHandler.removeCallbacks(reportRunnable)

        // 最后上报一次
        reportMetrics()

        if (config.debugMode) {
            android.util.Log.d("FluencyMonitor", "流畅性监控已停止")
        }
    }

    private fun reportMetrics() {
        if (frameTimeQueue.isEmpty()) return

        // 计算平均FPS
        val frameList = frameTimeQueue.toList()
        frameTimeQueue.clear()

        val avgFrameTime = frameList.average()
        val avgFps = if (avgFrameTime > 0) 1000f / avgFrameTime.toFloat() else 0f

        val data = FluencyData(
            timestamp = System.currentTimeMillis(),
            avgFps = String.format("%.2f", avgFps).toFloat(),
            jankyFrameCount = jankyFrameCount,
            maxFrameTimeMs = maxFrameTime,
            pageName = getCurrentPageName()
        )

        reporter.reportFluency(data)

        // 重置计数
        jankyFrameCount = 0
        maxFrameTime = 0
    }

    private fun getCurrentPageName(): String {
        // 简化实现，实际可通过ActivityLifecycleCallbacks获取
        return "CurrentActivity"
    }
}