package com.aditya.present.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aditya.present.data.PresentDatabase
import com.aditya.present.domain.AttendanceStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Handles Present / Absent / Cancel actions from the class notification.
 * Marks attendance via the DAO and dismisses the notification.
 */
class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ClassAlarmReceiver.ACTION_MARK) return

        val subjectId = intent.getLongExtra(EXTRA_SUBJECT_ID, -1)
        val slotId = intent.getLongExtra(EXTRA_SLOT_ID, -1)
        val dateMillis = intent.getLongExtra(EXTRA_DATE_MILLIS, System.currentTimeMillis())
        val statusStr = intent.getStringExtra(EXTRA_STATUS) ?: return
        val notifId = intent.getIntExtra(EXTRA_NOTIF_ID, -1)

        if (subjectId == -1L || slotId == -1L) return

        val status = runCatching { AttendanceStatus.valueOf(statusStr) }.getOrNull() ?: return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = PresentDatabase.get(context).dao()
                val startOfDay = startOfDay(dateMillis)
                val endOfDay = endOfDay(dateMillis)
                val existing = dao.getAttendanceForSlotOnDate(subjectId, slotId, startOfDay, endOfDay)
                if (existing != null) {
                    dao.updateAttendanceStatus(existing.id, status.name)
                } else {
                    dao.insertAttendance(
                        com.aditya.present.data.AttendanceEntity(
                            subjectId = subjectId,
                            date = dateMillis,
                            slotId = slotId,
                            status = status.name,
                            units = 1,
                            isAuto = false,
                        )
                    )
                }

                if (notifId != -1) {
                    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    manager.cancel(notifId)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun startOfDay(timestamp: Long): Long {
        val cal = java.util.Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    private fun endOfDay(timestamp: Long): Long {
        val cal = java.util.Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(java.util.Calendar.HOUR_OF_DAY, 23)
            set(java.util.Calendar.MINUTE, 59)
            set(java.util.Calendar.SECOND, 59)
            set(java.util.Calendar.MILLISECOND, 999)
        }
        return cal.timeInMillis
    }

    companion object {
        const val EXTRA_SUBJECT_ID = "subject_id"
        const val EXTRA_SLOT_ID = "slot_id"
        const val EXTRA_DATE_MILLIS = "date_millis"
        const val EXTRA_STATUS = "status"
        const val EXTRA_NOTIF_ID = "notif_id"
    }
}
