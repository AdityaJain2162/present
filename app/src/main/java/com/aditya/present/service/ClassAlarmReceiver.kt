package com.aditya.present.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.aditya.present.MainActivity
import com.aditya.present.R
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
 * Fired by [AlarmScheduler] when a class reminder is due.
 * Posts a notification with inline Present / Absent / Cancel actions,
 * then reschedules the alarm for the next week's occurrence.
 */
class ClassAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val slotId = intent.getLongExtra(EXTRA_SLOT_ID, -1)
        val subjectId = intent.getLongExtra(EXTRA_SUBJECT_ID, -1)
        val subjectName = intent.getStringExtra(EXTRA_SUBJECT_NAME) ?: return
        val subjectColor = intent.getIntExtra(EXTRA_SUBJECT_COLOR, 0xFF6750A4.toInt())
        val startTimeMinutes = intent.getIntExtra(EXTRA_START_TIME_MINUTES, 0)
        val dateMillis = intent.getLongExtra(EXTRA_DATE_MILLIS, System.currentTimeMillis())

        if (slotId == -1L || subjectId == -1L) return

        NotificationChannels.create(context)

        val notifId = slotId.toInt()

        val timeText = formatTime(startTimeMinutes)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPending = PendingIntent.getActivity(
            context, notifId, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        fun actionPending(status: AttendanceStatus): PendingIntent {
            val actionIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = ACTION_MARK
                putExtra(NotificationActionReceiver.EXTRA_SUBJECT_ID, subjectId)
                putExtra(NotificationActionReceiver.EXTRA_SLOT_ID, slotId)
                putExtra(NotificationActionReceiver.EXTRA_DATE_MILLIS, dateMillis)
                putExtra(NotificationActionReceiver.EXTRA_STATUS, status.name)
                putExtra(NotificationActionReceiver.EXTRA_NOTIF_ID, notifId)
            }
            return PendingIntent.getBroadcast(
                context, notifId * 10 + status.ordinal, actionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        val notification = NotificationCompat.Builder(context, NotificationChannels.CLASS_REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(subjectName)
            .setContentText(context.getString(R.string.notif_class_starting, timeText))
            .setColor(subjectColor)
            .setColorized(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openPending)
            .addAction(0, context.getString(R.string.status_present), actionPending(AttendanceStatus.PRESENT))
            .addAction(0, context.getString(R.string.status_absent), actionPending(AttendanceStatus.ABSENT))
            .addAction(0, context.getString(R.string.status_cancelled), actionPending(AttendanceStatus.CANCELLED))
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notifId, notification)

        // Reschedule for next week's occurrence
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = PresentDatabase.get(context).dao()
                val themeRepo = com.aditya.present.data.ThemeRepository(context)
                val prefs = themeRepo.themePrefs.first()
                if (!prefs.classNotificationsEnabled) return@launch

                val slot = dao.getAllSlots().find { it.id == slotId } ?: return@launch
                val subject = dao.getSubjectById(subjectId) ?: return@launch

                // Schedule for next week (7 days from the fired date)
                val nextDate = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 7)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                AlarmScheduler.scheduleClassReminder(
                    context = context,
                    slot = slot,
                    subject = subject,
                    dateMillis = nextDate.timeInMillis,
                    leadMinutes = prefs.notificationLeadMinutes,
                )
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun formatTime(startTimeMinutes: Int): String {
        val hour = startTimeMinutes / 60
        val minute = startTimeMinutes % 60
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return android.text.format.DateFormat.format("h:mm a", cal).toString()
    }

    companion object {
        const val EXTRA_SLOT_ID = "slot_id"
        const val EXTRA_SUBJECT_ID = "subject_id"
        const val EXTRA_SUBJECT_NAME = "subject_name"
        const val EXTRA_SUBJECT_COLOR = "subject_color"
        const val EXTRA_START_TIME_MINUTES = "start_time_minutes"
        const val EXTRA_DATE_MILLIS = "date_millis"
        const val ACTION_MARK = "com.aditya.present.ACTION_MARK"

        fun createIntent(
            context: Context,
            slotId: Long,
            subjectId: Long,
            subjectName: String,
            subjectColor: Int,
            startTimeMinutes: Int,
            dateMillis: Long,
        ): Intent = Intent(context, ClassAlarmReceiver::class.java).apply {
            putExtra(EXTRA_SLOT_ID, slotId)
            putExtra(EXTRA_SUBJECT_ID, subjectId)
            putExtra(EXTRA_SUBJECT_NAME, subjectName)
            putExtra(EXTRA_SUBJECT_COLOR, subjectColor)
            putExtra(EXTRA_START_TIME_MINUTES, startTimeMinutes)
            putExtra(EXTRA_DATE_MILLIS, dateMillis)
        }
    }
}
