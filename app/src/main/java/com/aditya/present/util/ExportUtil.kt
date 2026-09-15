package com.aditya.present.util

import android.content.Context
import android.net.Uri
import com.aditya.present.data.AcademicSessionEntity
import com.aditya.present.data.AttendanceEntity
import com.aditya.present.data.ClassSlotEntity
import com.aditya.present.data.PresentRepository
import com.aditya.present.data.SubjectEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * CSV export utility with formula-injection guarding (RFC 4180).
 * Exports sessions, subjects, class slots, and attendance records.
 */
object ExportUtil {

    private val dateFmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    /**
     * Escapes a CSV field per RFC 4180 and guards against formula injection.
     * Prefixes cells starting with =, +, -, @, \t, \r with a single quote.
     */
    private fun escapeCsv(value: String): String {
        val guarded = if (value.isNotEmpty() && value[0] in setOf('=', '+', '-', '@', '\t', '\r')) {
            "'$value"
        } else {
            value
        }
        if (guarded.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            return "\"${guarded.replace("\"", "\"\"")}\""
        }
        return guarded
    }

    private fun row(vararg fields: String): String {
        return fields.joinToString(",") { escapeCsv(it) }
    }

    suspend fun exportToCsv(context: Context, repository: PresentRepository, outputUri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val sessions = repository.getAllSessionsList()
                val subjects = repository.getAllSubjects()
                val slots = repository.getAllSlots()
                val attendance = repository.getAllAttendance()

                val csv = buildString {
                    // Sessions
                    appendLine("=== Sessions ===")
                    appendLine(row("ID", "Name", "Type", "Start Date", "End Date", "Target %", "Active"))
                    sessions.forEach { s ->
                        appendLine(row(
                            s.id.toString(),
                            s.name,
                            s.type,
                            dateFmt.format(Date(s.startDate)),
                            dateFmt.format(Date(s.endDate)),
                            s.targetAttendancePercent.toInt().toString(),
                            if (s.isActive) "Yes" else "No",
                        ))
                    }
                    appendLine()

                    // Subjects
                    appendLine("=== Subjects ===")
                    appendLine(row("ID", "Session ID", "Name", "Acronym", "Color", "Target %", "Total Units", "Teacher"))
                    subjects.forEach { s ->
                        appendLine(row(
                            s.id.toString(),
                            s.sessionId.toString(),
                            s.name,
                            s.acronym,
                            "#${Integer.toHexString(s.color).uppercase()}",
                            s.targetAttendancePercent.toInt().toString(),
                            s.totalUnits.toString(),
                            s.teacherName,
                        ))
                    }
                    appendLine()

                    // Class Slots
                    appendLine("=== Class Slots ===")
                    appendLine(row("ID", "Subject ID", "Day of Week", "Start Time (minutes)", "Units"))
                    slots.forEach { s ->
                        appendLine(row(
                            s.id.toString(),
                            s.subjectId.toString(),
                            s.dayOfWeek.toString(),
                            s.startTimeMinutes.toString(),
                            s.units.toString(),
                        ))
                    }
                    appendLine()

                    // Attendance
                    appendLine("=== Attendance ===")
                    appendLine(row("ID", "Subject ID", "Date", "Slot ID", "Status", "Units", "Auto"))
                    attendance.forEach { a ->
                        appendLine(row(
                            a.id.toString(),
                            a.subjectId.toString(),
                            dateFmt.format(Date(a.date)),
                            a.slotId?.toString() ?: "",
                            a.status,
                            a.units.toString(),
                            if (a.isAuto) "Yes" else "No",
                        ))
                    }
                }

                context.contentResolver.openOutputStream(outputUri)?.use { out ->
                    out.write(csv.toByteArray(Charsets.UTF_8))
                } ?: return@withContext false
                true
            } catch (e: Exception) {
                false
            }
        }
    }
}
