package com.dutyalarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.json.JSONArray
import org.json.JSONObject

object Scheduler {
    private fun pi(c: Context, i: Int, o: JSONObject?): PendingIntent {
        val n = Intent(c, AlarmReceiver::class.java)
        if (o != null) n.putExtra("title", o.getString("title")).putExtra("alarm", o.getBoolean("alarm"))
        return PendingIntent.getBroadcast(c, i, n, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    fun sync(c: Context, json: String) {
        val p = c.getSharedPreferences("d", 0)
        val am = c.getSystemService(AlarmManager::class.java)
        val old = JSONArray(p.getString("j", "[]")!!)
        for (i in 0 until old.length()) am.cancel(pi(c, i, null))
        p.edit().putString("j", json).apply()
        set(c, json)
    }

    fun set(c: Context, json: String) {
        val am = c.getSystemService(AlarmManager::class.java)
        val show = PendingIntent.getActivity(c, 999, Intent(c, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        val a = JSONArray(json)
        val now = System.currentTimeMillis()
        for (i in 0 until a.length()) {
            val o = a.getJSONObject(i)
            val t = o.getLong("t")
            if (t > now) am.setAlarmClock(AlarmManager.AlarmClockInfo(t, show), pi(c, i, o))
        }
    }
}

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        c.startForegroundService(Intent(c, AlarmService::class.java).putExtras(i))
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        Scheduler.set(c, c.getSharedPreferences("d", 0).getString("j", "[]")!!)
    }
}
