package com.example.famsafe

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.lifecycle.LiveData

class BatteryPercentageListener(private val context: Context) : LiveData<Int>() {

    private val batteryBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val batteryPercentage = (level.toFloat() / scale.toFloat() * 100).toInt()
                value = batteryPercentage
            }
        }
    }

    override fun onActive() {
        super.onActive()
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(batteryBroadcastReceiver, filter)
    }

    override fun onInactive() {
        super.onInactive()
        context.unregisterReceiver(batteryBroadcastReceiver)
    }
}
