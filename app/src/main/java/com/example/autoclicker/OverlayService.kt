package com.example.autoclicker

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.Button
import androidx.core.app.NotificationCompat

class OverlayService : Service() {

    private lateinit var wm: WindowManager
    private lateinit var overlay: View
    private var targetX = 0f
    private var targetY = 0f
    private var interval = 500L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(1, buildNotification())
        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        addOverlay()
    }

    private fun buildNotification(): Notification {
        val channelId = "clicker"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(channelId, "Clicker", NotificationManager.IMPORTANCE_LOW)
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(ch)
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Auto Clicker running")
            .setSmallIcon(android.R.drawable.ic_menu_myplaces)
            .build()
    }

    private fun addOverlay() {
        overlay = LayoutInflater.from(this).inflate(R.layout.overlay_button, null)
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 50
        params.y = 300

        wm.addView(overlay, params)

        overlay.setOnTouchListener(object : View.OnTouchListener {
            var initX = 0; var initY = 0
            var touchX = 0f; var touchY = 0f
            override fun onTouch(v: View, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initX = params.x; initY = params.y
                        touchX = e.rawX; touchY = e.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initX + (e.rawX - touchX).toInt()
                        params.y = initY + (e.rawY - touchY).toInt()
                        wm.updateViewLayout(overlay, params)
                        return true
                    }
                }
                return false
            }
        })

        overlay.findViewById<Button>(R.id.btnPick).setOnClickListener {
            targetX = params.x + overlay.width / 2f
            targetY = params.y + overlay.height / 2f
            android.widget.Toast.makeText(this,
                "Target: ${targetX.toInt()}, ${targetY.toInt()}",
                android.widget.Toast.LENGTH_SHORT).show()
        }

        overlay.findViewById<Button>(R.id.btnStart).setOnClickListener {
            ClickerEngine.start(targetX, targetY, interval)
        }

        overlay.findViewById<Button>(R.id.btnStop).setOnClickListener {
            ClickerEngine.stop()
        }
    }

    override fun onDestroy() {
        if (::overlay.isInitialized) wm.removeView(overlay)
        ClickerEngine.stop()
        super.onDestroy()
    }
}
