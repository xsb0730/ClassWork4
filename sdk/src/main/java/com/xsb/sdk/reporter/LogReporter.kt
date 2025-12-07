package com.xsb.sdk.reporter

import android.util.Log
import com.xsb.sdk.model.ANRData
import com.xsb.sdk.model.FluencyData

//日志上报实现
class LogReporter : IReporter {

    companion object {
        private const val TAG = "PerformanceMonitor"
    }

    override fun reportFluency(data: FluencyData) {
        Log.w(TAG, """
            ========== 流畅性报告 ==========
            时间段: ${data.timestamp}
            平均FPS: ${data.avgFps}
            卡顿次数: ${data.jankyFrameCount}
            最大帧耗时: ${data.maxFrameTimeMs}ms
            页面: ${data.pageName}
            ================================
        """.trimIndent())
    }

    override fun reportANR(data: ANRData) {
        Log.e(TAG, """
            ========== ANR报告 ==========
            检测时间: ${data.timestamp}
            主线程阻塞时长: ${data.blockTimeMs}ms
            页面: ${data.pageName}
            主线程堆栈:
            ${data.mainThreadStackTrace}
            ============================
        """.trimIndent())
    }
}