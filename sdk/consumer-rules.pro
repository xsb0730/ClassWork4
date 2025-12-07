# 保留SDK公开API
-keep class com.monitor.sdk.core.PerformanceMonitor { *; }
-keep class com.monitor.sdk.core.MonitorConfig { *; }
-keep interface com.monitor.sdk.reporter.IReporter { *; }
-keep class com.monitor.sdk.model.** { *; }

# 保留SDK内部实现（防止混淆导致问题）
-keep class com.monitor.sdk.** { *; }