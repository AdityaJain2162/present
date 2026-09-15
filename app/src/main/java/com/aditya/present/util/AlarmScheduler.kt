package com.aditya.present.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.aditya.present.data.ClassSlotEntity
import com.aditya.present.data.SubjectEntity
import com.aditya.present.service.AutoMarkReceiver
import com.aditya.present.service.ClassAlarmReceiver
import java.util.Calendar

/**
 * Schedules exact alarms for class slot reminders and the daily auto-mark.
 * Uses [AlarmManager.setExactAndAllowWhileIdle] so alarms fire in Doze.
 */
object AlarmScheduler {

    private const val AUTO_MARK_REQUEST_CODE = 99990

    /**
     * Schedule a reminder alarm for a single class slot on a specific date.
     * Fires [leadMinutes] before the slot start time.
     */
    fun scheduleClassReminder(
        context: Context,
        slot: ClassSlotEntity,
        subject: SubjectEntity,
        dateMillis: Long,
        leadMinutes: Int = 10,
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = computeTriggerTime(dateMillis, slot.startTimeMinutes, leadMinutes)
        if (triggerAt <= System.currentTimeMillis()) return // skip past times

        val intent = ClassAlarmReceiver.createIntent(
            context,
            slotId = slot.id,
            subjectId = slot.subjectId,
            subjectName = subject.name,
            subjectColor = subject.color,
            startTimeMinutes = slot.startTimeMinutes,
            dateMillis = dateMillis,
        )
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            slot.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    fun cancelClassReminder(context: Context, slotId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ClassAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            slotId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarmManager.cancel(pendingIntent)
    }

    /**
     * Schedule the daily auto-mark alarm at [hour]:00.
     * Repeats every day at the same time.
     */
    fun scheduleAutoMark(context: Context, hour: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, AutoMarkReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            AUTO_MARK_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent,
            )
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent,
            )
        }
    }

    fun cancelAutoMark(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AutoMarkReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            AUTO_MARK_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun computeTriggerTime(dateMillis: Long, startTimeMinutes: Int, leadMinutes: Int): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = dateMillis
            set(Calendar.HOUR_OF_DAY, startTimeMinutes / 60)
            set(Calendar.MINUTE, startTimeMinutes % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MINUTE, -leadMinutes)
        }
        return cal.timeInMillis
    }
}
