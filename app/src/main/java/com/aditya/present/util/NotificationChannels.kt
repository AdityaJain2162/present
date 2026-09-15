package com.aditya.present.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val CLASS_REMINDERS = "class_reminders"
    const val AUTO_MARK = "auto_mark"

    fun create(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        manager.createNotificationChannel(
            NotificationChannel(
                CLASS_REMINDERS,
                "Class Reminders",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Notifications before scheduled classes with quick mark actions"
                enableVibration(true)
            },
        )

        manager.createNotificationChannel(
            NotificationChannel(
                AUTO_MARK,
                "Auto Mark",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Daily auto-marking of unmarked classes"
            },
        )
    }
}
