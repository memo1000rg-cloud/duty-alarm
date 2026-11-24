package com.dutyalarm

import android.Manifest
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView

class MainActivity : Activity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        val w = WebView(this)
        w.settings.javaScriptEnabled = true
        w.settings.domStorageEnabled = true
        w.addJavascriptInterface(object {
            @JavascriptInterface
            fun sync(j: String) { Scheduler.sync(applicationContext, j) }
        }, "Android")
        w.loadUrl("file:///android_asset/index.html")
        setContentView(w)
    }
}
