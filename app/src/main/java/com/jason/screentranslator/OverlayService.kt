package com.jason.screentranslator

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class OverlayService : Service() {
    private lateinit var windowManager: WindowManager
    private var bubbleView: View? = null
    private var resultView: View? = null
    private var ocrEngine: ScreenCaptureOcrEngine? = null
    private val translator = TranslationPipeline()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())
        windowManager = getSystemService(WindowManager::class.java)
        if (intent?.action == ACTION_START) {
            val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
            val resultData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(EXTRA_RESULT_DATA, Intent::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(EXTRA_RESULT_DATA)
            }
            if (resultCode != 0 && resultData != null) {
                ocrEngine?.close()
                ocrEngine = ScreenCaptureOcrEngine(this, resultCode, resultData)
            }
            showBubble()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        serviceScope.cancel()
        ocrEngine?.close()
        removeViews()
        super.onDestroy()
    }

    private fun showBubble() {
        if (bubbleView != null) return
        val button = Button(this).apply {
            text = "译"
            textSize = 18f
            setOnClickListener { translateCurrentScreen() }
        }
        val params = overlayParams(width = 150, height = 150).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40
            y = 220
        }
        makeDraggable(button, params)
        bubbleView = button
        windowManager.addView(button, params)
    }

    private fun translateCurrentScreen() {
        showResult("正在识别当前屏幕……")
        serviceScope.launch {
            val recognizedText = ocrEngine?.recognizeCurrentScreen().orEmpty().ifBlank { "未识别到文字" }
            val translatedText = translator.translate(recognizedText)
            showResult(translatedText)
        }
    }

    private fun showResult(text: String) {
        resultView?.let { windowManager.removeView(it) }
        val resultText = TextView(this).apply {
            this.text = text
            textSize = 16f
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xDD222222.toInt())
            setPadding(28, 22, 28, 22)
        }
        val close = Button(this).apply {
            this.text = "关闭"
            setOnClickListener {
                resultView?.let { view -> windowManager.removeView(view) }
                resultView = null
            }
        }
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xDD222222.toInt())
            addView(resultText)
            addView(close)
        }
        val params = overlayParams(width = WindowManager.LayoutParams.MATCH_PARENT, height = WindowManager.LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            x = 0
            y = 80
        }
        resultView = layout
        windowManager.addView(layout, params)
    }

    private fun overlayParams(width: Int, height: Int): WindowManager.LayoutParams {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        return WindowManager.LayoutParams(
            width,
            height,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
    }

    private fun makeDraggable(view: View, params: WindowManager.LayoutParams) {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var moved = false
        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    moved = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    moved = true
                    windowManager.updateViewLayout(view, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!moved) view.performClick()
                    true
                }
                else -> false
            }
        }
    }

    private fun removeViews() {
        bubbleView?.let { windowManager.removeView(it) }
        resultView?.let { windowManager.removeView(it) }
        bubbleView = null
        resultView = null
    }

    private fun buildNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_menu_view)
        .setContentTitle("屏幕悬浮翻译运行中")
        .setContentText("点击悬浮球翻译当前屏幕")
        .setOngoing(true)
        .build()

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "屏幕悬浮翻译",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    companion object {
        const val ACTION_START = "com.jason.screentranslator.START"
        const val EXTRA_RESULT_CODE = "result_code"
        const val EXTRA_RESULT_DATA = "result_data"
        private const val CHANNEL_ID = "screen_translator_overlay"
        private const val NOTIFICATION_ID = 100
    }
}
