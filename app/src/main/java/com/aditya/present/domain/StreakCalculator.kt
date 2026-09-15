package com.aditya.present.domain

import com.aditya.present.data.AttendanceEntity
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Calculates attendance streaks from attendance entries.
 *
 * - **Current streak**: consecutive days with at least one PRESENT entry
 *   (no ABSENT in between), counting back from today.
 * - **Best streak**: the longest such run across all recorded history.
 * - **Perfect days**: days where every class was marked PRESENT.
 */
object StreakCalculator {

    data class StreakStats(
        val currentStreak: Int,
        val bestStreak: Int,
        val perfectDays: Int,
    )

    fun calculate(entries: List<AttendanceEntity>): StreakStats {
        if (entries.isEmpty()) return StreakStats(0, 0, 0)

        // Group entries by day (start-of-day timestamp)
        val byDay = entries.groupBy { startOfDay(it.date) }.toSortedMap()
        val dayStatuses = byDay.mapValues { (_, dayEntries) ->
            dayEntries.map { AttendanceStatus.valueOf(it.status) }
        }

        val perfectDays = dayStatuses.count { (_, statuses) ->
            statuses.isNotEmpty() && statuses.all { it == AttendanceStatus.PRESENT }
        }

        // Current streak: count back from today (or yesterday if nothing marked today yet)
        val today = startOfDay(System.currentTimeMillis())
        val currentStreak = countCurrentStreak(dayStatuses, today)

        // Best streak: scan all days
        val bestStreak = countBestStreak(dayStatuses)

        return StreakStats(currentStreak, bestStreak, perfectDays)
    }

    private fun countCurrentStreak(
        dayStatuses: Map<Long, List<AttendanceStatus>>,
        today: Long,
    ): Int {
        var streak = 0
        var day = today

        // If nothing marked today, start from yesterday
        if (dayStatuses[day]?.isEmpty() != false) {
            day -= TimeUnit.DAYS.toMillis(1)
        }

        while (true) {
            val statuses = dayStatuses[day]
            if (statuses.isNullOrEmpty()) break
            // Streak breaks if any ABSENT on this day
            if (statuses.any { it == AttendanceStatus.ABSENT }) break
            // Must have at least one PRESENT
            if (statuses.any { it == AttendanceStatus.PRESENT }) {
                streak++
            } else {
                break
            }
            day -= TimeUnit.DAYS.toMillis(1)
        }
        return streak
    }

    private fun countBestStreak(dayStatuses: Map<Long, List<AttendanceStatus>>): Int {
        var best = 0
        var current = 0
        var prevDay = -1L

        for ((day, statuses) in dayStatuses) {
            if (prevDay != -1L && day - prevDay != TimeUnit.DAYS.toMillis(1)) {
                current = 0 // gap breaks streak
            }

            if (statuses.isNotEmpty() &&
                statuses.none { it == AttendanceStatus.ABSENT } &&
                statuses.any { it == AttendanceStatus.PRESENT }
            ) {
                current++
                if (current > best) best = current
            } else {
                current = 0
            }
            prevDay = day
        }
        return best
    }

    private fun startOfDay(timestamp: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }
}
