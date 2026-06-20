package com.jason.screentranslator

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScreenCaptureOcrEngine(
    private val context: Context,
    resultCode: Int,
    resultData: Intent
) {
    private val projection: MediaProjection

    init {
        val manager = context.getSystemService(MediaProjectionManager::class.java)
        projection = manager.getMediaProjection(resultCode, resultData)
    }

    suspend fun recognizeCurrentScreen(): String = withContext(Dispatchers.Default) {
        "安装兼容版已启动：悬浮窗和屏幕捕获授权流程可测试；本版本暂时关闭本地 OCR 大模型，用于排查手机安装器是否被大体积 ML Kit 包触发闪退。"
    }

    fun close() {
        projection.stop()
    }
}
