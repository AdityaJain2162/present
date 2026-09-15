package com.aditya.present.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aditya.present.data.PresentDatabase
import com.aditya.present.data.ThemeRepository
import com.aditya.present.util.AlarmScheduler
import com.aditya.present.util.NotificationChannels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Re-schedules all class reminder alarms and the daily auto-mark after device reboot.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_LOCKED_BOOT_COMPLETED &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON"
        ) return

        NotificationChannels.create(context)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = PresentDatabase.get(context).dao()
                val themeRepo = ThemeRepository(context)
                val currentPrefs = themeRepo.themePrefs.first()

                // Re-schedule class reminders for ALL slots (not just today)
                if (currentPrefs.classNotificationsEnabled) {
                    val allSlots = dao.getAllSlots()
                    val today = Calendar.getInstance()
                    for (slot in allSlots) {
                        val subject = dao.getSubjectById(slot.subjectId) ?: continue

                        // Find the next occurrence of this day of week
                        val todayDayOfWeek = today.get(Calendar.DAY_OF_WEEK)
                        var daysUntil = (slot.dayOfWeek - todayDayOfWeek + 7) % 7
                        if (daysUntil == 0) {
                            // Today — check if the class hasn't started yet
                            val classTime = Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, slot.startTimeMinutes / 60)
                                set(Calendar.MINUTE, slot.startTimeMinutes % 60)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            if (classTime.timeInMillis <= System.currentTimeMillis()) {
                                daysUntil = 7
                            }
                        }
                        val nextDate = Calendar.getInstance().apply {
                            add(Calendar.DAY_OF_YEAR, daysUntil)
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
                            leadMinutes = currentPrefs.notificationLeadMinutes,
                        )
                    }
                }

                // Re-schedule auto-mark only if enabled
                if (currentPrefs.autoMarkEnabled) {
                    AlarmScheduler.scheduleAutoMark(context, currentPrefs.autoMarkHour)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
