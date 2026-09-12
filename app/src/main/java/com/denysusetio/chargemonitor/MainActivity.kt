package com.denysusetio.chargemonitor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvCurrent: TextView
    private lateinit var tvVoltage: TextView
    private lateinit var tvPower: TextView
    private lateinit var tvStatus: TextView

    private lateinit var batteryManager: BatteryManager
    private val handler = Handler(Looper.getMainLooper())
    private var lastVoltageMv: Int = 0
    private var isCharging: Boolean = false

    // Poll BatteryManager tiap 1 detik untuk update arus real-time
    private val pollRunnable = object : Runnable {
        override fun run() {
            updateCurrentReading()
            handler.postDelayed(this, 1000)
        }
    }

    // Broadcast ini yang kasih info voltage & status charging (sticky, jarang berubah)
    private val batteryStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            lastVoltageMv = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
            val sourceLabel = when (plugged) {
                BatteryManager.BATTERY_PLUGGED_AC -> "Charger AC"
                BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
                else -> "Tidak terhubung"
            }
            tvStatus.text = if (isCharging) "Status: Charging ($sourceLabel)" else "Status: Tidak charging"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvCurrent = findViewById(R.id.tvCurrent)
        tvVoltage = findViewById(R.id.tvVoltage)
        tvPower = findViewById(R.id.tvPower)
        tvStatus = findViewById(R.id.tvStatus)

        batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(batteryStatusReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        handler.post(pollRunnable)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(batteryStatusReceiver)
        handler.removeCallbacks(pollRunnable)
    }

    private fun updateCurrentReading() {
        // Nilai dari sistem dalam microampere (µA). Sebagian device melaporkan
        // negatif saat charging, sebagian lagi positif — makanya dipakai absolut.
        val currentMicroAmp = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        val currentMa = currentMicroAmp / 1000.0
        val currentAbsMa = kotlin.math.abs(currentMa)

        val voltageV = lastVoltageMv / 1000.0
        val powerW = (currentAbsMa / 1000.0) * voltageV

        tvCurrent.text = String.format("Arus: %.0f mA", currentAbsMa)
        tvVoltage.text = String.format("Tegangan: %.2f V", voltageV)
        tvPower.text = String.format("Daya: %.2f W", powerW)
    }
}
