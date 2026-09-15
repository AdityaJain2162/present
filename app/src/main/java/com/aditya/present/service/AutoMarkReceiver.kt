package com.aditya.present.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.aditya.present.MainActivity
import com.aditya.present.R
import com.aditya.present.data.AttendanceEntity
import com.aditya.present.data.PresentDatabase
import com.aditya.present.domain.AttendanceStatus
import com.aditya.present.util.AlarmScheduler
import com.aditya.present.util.NotificationChannels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Fires daily at the configured auto-mark hour (default 10 PM).
 * Marks all unmarked classes from today as ABSENT (isAuto = true),
 * then posts a summary notification.
 */
class AutoMarkReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        NotificationChannels.create(context)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = PresentDatabase.get(context).dao()
                val themeRepo = com.aditya.present.data.ThemeRepository(context)
                val prefs = themeRepo.themePrefs.first()
                val today = Calendar.getInstance()
                val startOfDay = startOfDay(today)
                val endOfDay = endOfDay(today)

                // Get today's day of week
                val dayOfWeek = today.get(Calendar.DAY_OF_WEEK)

                // Skip weekends — no classes to auto-mark
                val todayName = dayName(dayOfWeek)
                val weekendDays = prefs.weekendDays.split(",").filter { it.isNotBlank() }.toSet()
                if (todayName in weekendDays) {
                    // Still schedule next day's alarm
                    AlarmScheduler.scheduleAutoMark(context, prefs.autoMarkHour)
                    return@launch
                }

                // Get all slots for today
                val slots = dao.getSlotsForDay(dayOfWeek).first()

                var markedCount = 0
                for (slot in slots) {
                    // Check if attendance already exists for this slot today
                    val existing = dao.getAttendanceForSlotOnDate(
                        slot.subjectId, slot.id, startOfDay, endOfDay
                    )
                    if (existing == null) {
                        dao.insertAttendance(
                            AttendanceEntity(
                                subjectId = slot.subjectId,
                                date = System.currentTimeMillis(),
                                slotId = slot.id,
                                status = AttendanceStatus.ABSENT.name,
                                units = slot.units,
                                isAuto = true,
                            )
                        )
                        markedCount++
                    }
                }

                if (markedCount > 0) {
                    postSummaryNotification(context, markedCount)
                }

                // Schedule next day's auto-mark
                AlarmScheduler.scheduleAutoMark(context, prefs.autoMarkHour)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun postSummaryNotification(context: Context, count: Int) {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPending = android.app.PendingIntent.getActivity(
            context, 0, openIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, NotificationChannels.AUTO_MARK)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.notif_auto_mark_title))
            .setContentText(context.getString(R.string.notif_auto_mark_body, count))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setContentIntent(openPending)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(AUTO_MARK_NOTIF_ID, notification)
    }

    private fun startOfDay(cal: Calendar): Long {
        val c = cal.clone() as Calendar
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    private fun endOfDay(cal: Calendar): Long {
        val c = cal.clone() as Calendar
        c.set(Calendar.HOUR_OF_DAY, 23)
        c.set(Calendar.MINUTE, 59)
        c.set(Calendar.SECOND, 59)
        c.set(Calendar.MILLISECOND, 999)
        return c.timeInMillis
    }

    private fun dayName(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            Calendar.SUNDAY -> "SUNDAY"
            Calendar.MONDAY -> "MONDAY"
            Calendar.TUESDAY -> "TUESDAY"
            Calendar.WEDNESDAY -> "WEDNESDAY"
            Calendar.THURSDAY -> "THURSDAY"
            Calendar.FRIDAY -> "FRIDAY"
            Calendar.SATURDAY -> "SATURDAY"
            else -> ""
        }
    }

    companion object {
        private const val AUTO_MARK_NOTIF_ID = 88888
    }
}
