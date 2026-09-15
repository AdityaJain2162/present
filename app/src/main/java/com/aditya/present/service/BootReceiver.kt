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

                // Re-schedule today's class reminders
                val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
                val slots = dao.getSlotsForDay(today).first()
                for (slot in slots) {
                    val subject = dao.getSubjectById(slot.subjectId) ?: continue
                    AlarmScheduler.scheduleClassReminder(
                        context = context,
                        slot = slot,
                        subject = subject,
                        dateMillis = System.currentTimeMillis(),
                    )
                }

                // Re-schedule auto-mark
                AlarmScheduler.scheduleAutoMark(context, currentPrefs.autoMarkHour)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
