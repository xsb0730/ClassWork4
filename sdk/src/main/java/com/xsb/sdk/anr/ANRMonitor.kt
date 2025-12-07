package com.xsb.sdk.anr

// ANR监控核心
import android.os.Handler
import android.os.Looper
import com.xsb.sdk.core.MonitorConfig
import com.xsb.sdk.model.ANRData
import com.xsb.sdk.reporter.IReporter
import java.util.concurrent.atomic.AtomicLong

class ANRMonitor(
    private val config: MonitorConfig,
    private val reporter: IReporter
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var watchDogThread: Thread? = null
    private var isMonitoring = false

    // 心跳时间戳
    private val heartbeatTime = AtomicLong(0L)

    private val heartbeatRunnable = Runnable {
        heartbeatTime.set(System.currentTimeMillis())
    }

    fun start() {
        if (isMonitoring) return

        isMonitoring = true
        heartbeatTime.set(System.currentTimeMillis())

        // 启动看门狗线程
        watchDogThread = Thread {
            while (isMonitoring) {
                try {
                    Thread.sleep(config.anrCheckInterval)

                    val lastHeartbeat = heartbeatTime.get()
                    val currentTime = System.currentTimeMillis()
                    val blockTime = currentTime - lastHeartbeat

                    // 检测ANR
                    if (blockTime >= config.anrThresholdMs) {
                        val mainThread = Looper.getMainLooper().thread
                        val stackTrace = mainThread.stackTrace.joinToString("\n") {
                            "    at $it"
                        }

                        val anrData = ANRData(
                            timestamp = currentTime,
                            blockTimeMs = blockTime,
                            pageName = getCurrentPageName(),
                            mainThreadStackTrace = stackTrace
                        )

                        reporter.reportANR(anrData)

                        // 重置心跳
                        heartbeatTime.set(currentTime)
                    }

                    // 发送心跳
                    mainHandler.post(heartbeatRunnable)

                } catch (e: InterruptedException) {
                    break
                }
            }
        }.apply {
            name = "ANR-WatchDog"
            isDaemon = true
            start()
        }

        if (config.debugMode) {
            android.util.Log.d("ANRMonitor", "ANR监控已启动")
        }
    }

    fun stop() {
        if (!isMonitoring) return

        isMonitoring = false
        watchDogThread?.interrupt()
        watchDogThread = null
        mainHandler.removeCallbacks(heartbeatRunnable)

        if (config.debugMode) {
            android.util.Log.d("ANRMonitor", "ANR监控已停止")
        }
    }

    private fun getCurrentPageName(): String {
        return "CurrentActivity"
    }
}