
# 📊 Android Performance Monitor SDK

一个轻量级的 Android 性能监控 SDK，用于实时检测应用的流畅性和 ANR（Application Not Responding）问题。

##  特性

-  **流畅性监控**
  - 基于 Choreographer 的高精度帧率监控
  - 实时 FPS 计算
  - 卡顿帧检测（可自定义阈值）
  - 周期性性能报告

-  **ANR 检测**
  - Watchdog 线程监控主线程状态
  - 可配置的 ANR 阈值（默认 5 秒）
  - 自动捕获主线程堆栈信息
  - 精确定位阻塞代码位置

-  **灵活配置**
  - 可独立启用/禁用各监控模块
  - 自定义阈值和上报间隔
  - 支持自定义上报器接口
  - 调试模式支持


##  Demo 预览

Demo 应用提供了完整的功能测试：

- ✅ 卡顿测试（100ms 主线程阻塞）
- ✅ ANR 测试（5 秒主线程阻塞）
- ✅ 重度动画测试（模拟复杂渲染场景）
- ✅ 启动/停止监控控制

##  快速开始

### 1. 集成 SDK

#### 添加依赖

在项目的 `settings.gradle.kts` 中添加 SDK 模块：

```kotlin
include(":app", ":sdk")
```

在 `app/build.gradle.kts` 中添加依赖：

```kotlin
dependencies {
    implementation(project(":sdk"))
    // ... 其他依赖
}
```

### 2. 初始化 SDK

创建自定义 Application 类：

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 配置监控参数
        val config = MonitorConfig(
            enableFluencyMonitor = true,      // 启用流畅性监控
            fluencyThresholdMs = 16,          // 16ms = 一帧，超过则视为卡顿
            fluencyReportInterval = 5000,     // 每 5 秒上报一次
            enableANRMonitor = true,          // 启用 ANR 监控
            anrThresholdMs = 5000,            // 主线程阻塞超过 5 秒视为 ANR
            anrCheckInterval = 1000,          // 每秒检查一次
            debugMode = true                  // 开启调试日志
        )
        
        // 初始化 SDK
        PerformanceMonitor.init(
            app = this,
            config = config
        )
        
        // 启动监控
        PerformanceMonitor.start()
    }
}
```

在 `AndroidManifest.xml` 中注册：

```xml
<application
    android:name=".MyApplication"
    ...>
    ...
</application>
```


### 3. 查看监控报告

监控数据会通过 Logcat 输出，过滤标签 `PerformanceMonitor`：

#### 流畅性报告示例

```log
W/PerformanceMonitor: ========== 流畅性报告 ==========
W/PerformanceMonitor: 时间段: 1765098040000
W/PerformanceMonitor: 平均FPS: 55.23
W/PerformanceMonitor: 卡顿次数: 3
W/PerformanceMonitor: 最大帧耗时: 105ms
W/PerformanceMonitor: 页面: MainActivity
W/PerformanceMonitor: ================================
```

#### ANR 报告示例

```log
E/PerformanceMonitor: ========== ANR报告 ==========
E/PerformanceMonitor: 检测时间: 1765098035985
E/PerformanceMonitor: 主线程阻塞时长: 5002ms
E/PerformanceMonitor: 页面: MainActivity
E/PerformanceMonitor: 主线程堆栈:
E/PerformanceMonitor:     at com.example.MainActivity.heavyOperation(MainActivity.kt:104)
E/PerformanceMonitor:     at android.view.View.performClick(View.java:7512)
E/PerformanceMonitor:     ...
E/PerformanceMonitor: ============================
```

##  API 文档

### MonitorConfig

配置监控参数：

```kotlin
data class MonitorConfig(
    // 流畅性监控配置
    val enableFluencyMonitor: Boolean = true,     // 是否启用流畅性监控
    val fluencyThresholdMs: Long = 16,            // 卡顿阈值（毫秒）
    val fluencyReportInterval: Long = 5000,       // 上报间隔（毫秒）
    
    // ANR 监控配置
    val enableANRMonitor: Boolean = true,         // 是否启用 ANR 监控
    val anrThresholdMs: Long = 5000,              // ANR 阈值（毫秒）
    val anrCheckInterval: Long = 1000,            // 检查间隔（毫秒）
    
    // 通用配置
    val debugMode: Boolean = false                // 调试模式
)
```

### PerformanceMonitor

主要 API：

```kotlin
// 初始化 SDK
PerformanceMonitor.init(
    app: Application,                              // Application 实例
    config: MonitorConfig = MonitorConfig(),       // 配置（可选）
    reporter: IReporter = LogReporter()            // 自定义上报器（可选）
)

// 启动监控
PerformanceMonitor.start()

// 停止监控
PerformanceMonitor.stop()
```

### 自定义上报器

实现 `IReporter` 接口来自定义数据上报：

```kotlin
class CustomReporter : IReporter {
    override fun reportFluency(data: FluencyData) {
        // 上报到自己的服务器
        uploadToServer(data)
    }
    
    override fun reportANR(data: ANRData) {
        // 上报到自己的服务器
        uploadToServer(data)
    }
}

// 使用自定义上报器
PerformanceMonitor.init(
    app = this,
    reporter = CustomReporter()
)
```

### 数据模型

#### FluencyData

```kotlin
data class FluencyData(
    val timestamp: Long,           // 时间戳
    val avgFps: Float,             // 平均 FPS
    val jankyFrameCount: Int,      // 卡顿帧数量
    val maxFrameTimeMs: Long,      // 最大帧耗时（毫秒）
    val pageName: String           // 页面名称
)
```

#### ANRData

```kotlin
data class ANRData(
    val timestamp: Long,                // 时间戳
    val blockTimeMs: Long,              // 阻塞时长（毫秒）
    val pageName: String,               // 页面名称
    val mainThreadStackTrace: String    // 主线程堆栈信息
)
```

##  项目结构

```
ClassWork4/
├── app/                              # Demo 应用
│   └── src/
│       └── main/
│           ├── java/com/xsb/classwork4/
│           │   ├── MyApplication.kt  # 应用入口（SDK 初始化）
│           │   └── MainActivity.kt   # 主界面（测试功能）
│           └── res/
│               └── layout/
│                   └── activity_main.xml
│
├── sdk/                              # 性能监控 SDK
│   └── src/
│       └── main/
│           └── java/com/xsb/sdk/
│               ├── core/             # 核心模块
│               │   ├── MonitorConfig.kt        # 配置类
│               │   └── PerformanceMonitor.kt   # SDK 主入口
│               │
│               ├── fluency/          # 流畅性监控模块
│               │   └── FluencyMonitor.kt       # FPS 监控实现
│               │
│               ├── anr/              # ANR 监控模块
│               │   └── ANRMonitor.kt           # ANR 检测实现
│               │
│               ├── reporter/         # 上报模块
│               │   └── IReporter.kt            # 上报接口 + 默认实现
│               │
│               └── model/            # 数据模型
│                   ├── FluencyData.kt          # 流畅性数据
│                   └── ANRData.kt              # ANR 数据
│
├── build.gradle.kts                  # 项目级构建配置
├── settings.gradle.kts               # 项目设置
└── README.md                         # 本文件
```

##  技术实现

### 流畅性监控原理

使用 Android 的 `Choreographer` 监听每一帧的绘制：

```kotlin
Choreographer.getInstance().postFrameCallback { frameTimeNanos ->
    // 计算帧间隔
    val frameTimeMs = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000
    
    // 判断是否卡顿
    if (frameTimeMs > fluencyThresholdMs) {
        jankyFrameCount++
    }
    
    // 持续监听下一帧
    Choreographer.getInstance().postFrameCallback(this)
}
```

### ANR 监控原理

使用独立的 Watchdog 线程监控主线程心跳：

```kotlin
// 主线程定期更新心跳时间
mainHandler.post {
    heartbeatTime.set(System.currentTimeMillis())
}

// Watchdog 线程检查心跳
Thread {
    while (isMonitoring) {
        val blockTime = System.currentTimeMillis() - heartbeatTime.get()
        if (blockTime >= anrThresholdMs) {
            // 检测到 ANR，捕获堆栈
            val stackTrace = Looper.getMainLooper().thread.stackTrace
            reportANR(stackTrace)
        }
        Thread.sleep(checkInterval)
    }
}.start()
```

##  性能优化

- ✅ **低开销**：使用系统回调机制，不引入额外轮询
- ✅ **异步处理**：ANR 检测在独立线程，不影响主线程
- ✅ **内存友好**：使用 `ConcurrentLinkedQueue` 管理帧数据，自动清理
- ✅ **可配置**：所有阈值和间隔可自定义，平衡精度和性能

##  使用场景

### 开发阶段

- 识别代码中的性能瓶颈
- 验证性能优化效果
- 发现潜在的 ANR 风险

### 测试阶段

- 压力测试下的性能表现
- 不同设备的适配验证
- 长时间运行的稳定性测试

### 生产环境

- 实时监控线上应用性能
- 收集用户端性能数据
- 快速定位性能问题


##  构建和运行

### 环境要求

- Android Studio Hedgehog | 2023.1.1+
- Kotlin 1.9+
- Gradle 8.0+
- Android SDK 21+（Android 5.0+）

```

### 运行 Demo 应用

1. 在 Android Studio 中打开项目
2. 选择运行配置 `app`
3. 点击运行按钮或按 `Shift + F10`
4. 在设备上测试各项功能
5. 查看 Logcat 输出（过滤 `PerformanceMonitor`）

---


