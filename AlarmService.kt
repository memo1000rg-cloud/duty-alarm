package com.dutyalarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper

class AlarmService : Service() {
    private var mp: MediaPlayer? = null
    private val h = Handler(Looper.getMainLooper())

    override fun onBind(i: Intent?): IBinder? = null

    private fun quit() {
        h.removeCallbacksAndMessages(null)
        mp?.release(); mp = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onStartCommand(i: Intent?, f: Int, id: Int): Int {
        if (i?.action == "stop") { quit(); return START_NOT_STICKY }
        val alarm = i?.getBooleanExtra("alarm", true) ?: true
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(NotificationChannel("a", "المنبه", NotificationManager.IMPORTANCE_HIGH))
        val fl = PendingIntent.FLAG_IMMUTABLE
        val open = PendingIntent.getActivity(this, 2, Intent(this, MainActivity::class.java), fl)
        val stop = PendingIntent.getService(this, 1, Intent(this, AlarmService::class.java).setAction("stop"), fl)
        val n = Notification.Builder(this, "a")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(i?.getStringExtra("title") ?: "منبه الدوام")
            .setContentIntent(open).setFullScreenIntent(open, true)
            .addAction(Notification.Action.Builder(0, "إيقاف", stop).build())
            .build()
        startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        mp?.release(); mp = null
        if (alarm) {
            mp = MediaPlayer().apply {
                setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                setDataSource(this@AlarmService, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                isLooping = true; prepare(); start()
            }
            h.postDelayed({ quit() }, 120000)
        } else {
            stopForeground(STOP_FOREGROUND_DETACH); stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() { mp?.release(); super.onDestroy() }
}
