package com.jason.screentranslator.probe

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView

class ProbeActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply {
            text = "安装诊断 APK 已成功打开\n\n如果你能看到这行字，说明手机可以安装我们生成的 APK。"
            textSize = 20f
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
        })
    }
}
