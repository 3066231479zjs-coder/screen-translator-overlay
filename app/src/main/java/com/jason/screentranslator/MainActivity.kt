package com.jason.screentranslator

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val screenCaptureRequestCode = 1001
    private lateinit var statusView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun buildUi() {
        statusView = TextView(this).apply {
            textSize = 16f
            setPadding(0, 0, 0, 24)
        }

        val overlayButton = Button(this).apply {
            text = "1. 开启悬浮窗权限"
            setOnClickListener { requestOverlayPermission() }
        }
        val notificationButton = Button(this).apply {
            text = "2. 开启通知权限（Android 13+）"
            setOnClickListener { requestNotificationPermission() }
        }
        val captureButton = Button(this).apply {
            text = "3. 授权屏幕捕获并启动"
            setOnClickListener { requestScreenCapture() }
        }
        val stopButton = Button(this).apply {
            text = "停止悬浮窗"
            setOnClickListener {
                stopService(Intent(this@MainActivity, OverlayService::class.java))
                Toast.makeText(this@MainActivity, "已停止", Toast.LENGTH_SHORT).show()
            }
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(40, 80, 40, 40)
            addView(statusView)
            addView(overlayButton)
            addView(notificationButton)
            addView(captureButton)
            addView(stopButton)
        }
        setContentView(layout)
        updateStatus()
    }

    private fun updateStatus() {
        val overlay = if (Settings.canDrawOverlays(this)) "已开启" else "未开启"
        statusView.text = "屏幕悬浮翻译 v0.1\n\n悬浮窗权限：$overlay\n\n使用方法：授权后切到任意页面，点击悬浮球，当前版本会显示可用流程占位；下一步接入真实截图 OCR 与翻译。"
    }

    private fun requestOverlayPermission() {
        if (Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "悬浮窗权限已开启", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivity(intent)
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 2001)
        } else {
            Toast.makeText(this, "当前系统不需要单独通知权限", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestScreenCapture() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "请先开启悬浮窗权限", Toast.LENGTH_SHORT).show()
            requestOverlayPermission()
            return
        }
        val manager = getSystemService(MediaProjectionManager::class.java)
        startActivityForResult(manager.createScreenCaptureIntent(), screenCaptureRequestCode)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == screenCaptureRequestCode && resultCode == RESULT_OK && data != null) {
            val serviceIntent = Intent(this, OverlayService::class.java).apply {
                action = OverlayService.ACTION_START
                putExtra(OverlayService.EXTRA_RESULT_CODE, resultCode)
                putExtra(OverlayService.EXTRA_RESULT_DATA, data)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            Toast.makeText(this, "悬浮球已启动", Toast.LENGTH_SHORT).show()
        } else if (requestCode == screenCaptureRequestCode) {
            Toast.makeText(this, "未授权屏幕捕获", Toast.LENGTH_SHORT).show()
        }
    }
}
