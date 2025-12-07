package com.xsb.classwork4

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.xsb.sdk.core.PerformanceMonitor
import com.xsb.classwork4.databinding.ActivityMainBinding
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var heavyAnimator: ValueAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化 PerformanceMonitor
        initPerformanceMonitor()

        setupViews()
        setupListeners()
    }

    /**
     * 初始化性能监控SDK
     */
    private fun initPerformanceMonitor() {
        val config = com.xsb.sdk.core.MonitorConfig(
            enableFluencyMonitor = true,
            fluencyThresholdMs = 16,
            fluencyReportInterval = 5000,
            enableANRMonitor = true,
            anrThresholdMs = 5000,
            anrCheckInterval = 1000,
            debugMode = true  // 开启调试模式，可以看到更多日志
        )

        PerformanceMonitor.init(
            app = application,
            config = config
        )

        // 自动启动监控
        PerformanceMonitor.start()
    }

    private fun setupViews() {
        supportActionBar?.title = "性能监控SDK测试"
    }

    private fun setupListeners() {
        // 测试卡顿
        binding.btnTestJanky.setOnClickListener {
            testJankyFrame()
        }

        // 测试ANR
        binding.btnTestANR.setOnClickListener {
            testANR()
        }

        // 测试重度动画
        binding.btnHeavyAnimation.setOnClickListener {
            toggleHeavyAnimation()
        }

        // 停止监控
        binding.btnStopMonitor.setOnClickListener {
            PerformanceMonitor.stop()
            updateStatus("✋ 监控已停止")
        }

        // 启动监控
        binding.btnStartMonitor.setOnClickListener {
            PerformanceMonitor.start()
            updateStatus("▶️ 监控运行中")
        }
    }

    /**
     * 测试卡顿帧
     */
    private fun testJankyFrame() {
        updateStatus("⚠️ 触发卡顿测试...")

        // 主线程睡眠100ms，导致掉帧
        Thread.sleep(100)

        updateStatus("✅ 卡顿测试完成 (100ms阻塞)")
    }

    /**
     * 测试ANR
     */
    private fun testANR() {
        updateStatus("⚠️ 触发ANR测试中...\n(主线程将阻塞5秒)")

        // 阻塞主线程5秒
        Thread.sleep(5000)

        updateStatus("✅ ANR测试完成")
    }

    /**
     * 切换重度动画
     */
    private fun toggleHeavyAnimation() {
        if (heavyAnimator?.isRunning == true) {
            stopHeavyAnimation()
        } else {
            startHeavyAnimation()
        }
    }

    /**
     * 启动重度动画
     */
    private fun startHeavyAnimation() {
        heavyAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 10000
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE

            addUpdateListener { animation ->
                val rotation = animation.animatedValue as Float

                // 执行复杂动画
                binding.tvTitle.rotation = rotation
                binding.tvTitle.scaleX = 1f + (rotation / 360f) * 0.3f
                binding.tvTitle.scaleY = 1f + (rotation / 360f) * 0.3f

                // 颜色动画
                val color = Color.HSVToColor(floatArrayOf(rotation, 0.8f, 0.9f))
                binding.tvTitle.setTextColor(color)

                // 模拟重度计算
                repeat(200) {
                    sqrt(it.toDouble())
                }
            }

            start()
        }

        binding.btnHeavyAnimation.text = "停止重度动画"
        updateStatus("🎬 重度动画运行中...")
    }

    /**
     * 停止重度动画
     */
    private fun stopHeavyAnimation() {
        heavyAnimator?.cancel()
        heavyAnimator = null

        // 重置UI
        binding.tvTitle.rotation = 0f
        binding.tvTitle.scaleX = 1f
        binding.tvTitle.scaleY = 1f
        binding.tvTitle.setTextColor(getColor(android.R.color.white))

        binding.btnHeavyAnimation.text = "测试重度动画"
        updateStatus("⏸️ 动画已停止")
    }

    /**
     * 更新状态文本
     */
    private fun updateStatus(status: String) {
        binding.tvStatus.text = getString(R.string.status_template, status)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopHeavyAnimation()
    }
}