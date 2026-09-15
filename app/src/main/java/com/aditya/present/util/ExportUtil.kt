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

    suspend fun importFromCsv(context: Context, repository: PresentRepository, inputUri: Uri): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val content = context.contentResolver.openInputStream(inputUri)?.use { it.readBytes().toString(Charsets.UTF_8) }
                    ?: return@withContext Result.failure(Exception("Cannot read file"))

                // Parse CSV sections
                val lines = content.lines().filter { it.isNotBlank() }
                var section = ""
                val sessions = mutableListOf<AcademicSessionEntity>()
                val subjects = mutableListOf<SubjectEntity>()
                val slots = mutableListOf<ClassSlotEntity>()
                val attendance = mutableListOf<AttendanceEntity>()

                for (line in lines) {
                    if (line.startsWith("=== ")) {
                        section = line.removePrefix("=== ").removeSuffix(" ===")
                        continue
                    }
                    if (line.startsWith("ID,") || line.startsWith("id,")) continue // skip headers

                    val fields = parseCsvRow(line)
                    when (section) {
                        "Sessions" -> {
                            if (fields.size >= 7) {
                                sessions.add(AcademicSessionEntity(
                                    id = fields[0].toLongOrNull() ?: 0,
                                    name = fields[1],
                                    type = fields[2],
                                    startDate = dateFmt.parse(fields[3])?.time ?: 0L,
                                    endDate = dateFmt.parse(fields[4])?.time ?: 0L,
                                    targetAttendancePercent = fields[5].toFloatOrNull() ?: 75f,
                                    isActive = fields[6].equals("Yes", ignoreCase = true),
                                ))
                            }
                        }
                        "Subjects" -> {
                            if (fields.size >= 8) {
                                val colorStr = fields[4].removePrefix("#")
                                subjects.add(SubjectEntity(
                                    id = fields[0].toLongOrNull() ?: 0,
                                    sessionId = fields[1].toLongOrNull() ?: 0,
                                    name = fields[2],
                                    acronym = fields[3],
                                    color = colorStr.toLongOrNull(16)?.toInt() ?: 0xFF006A6A.toInt(),
                                    targetAttendancePercent = fields[5].toFloatOrNull() ?: 75f,
                                    totalUnits = fields[6].toIntOrNull() ?: 1,
                                    teacherName = fields[7],
                                ))
                            }
                        }
                        "Class Slots" -> {
                            if (fields.size >= 5) {
                                slots.add(ClassSlotEntity(
                                    id = fields[0].toLongOrNull() ?: 0,
                                    subjectId = fields[1].toLongOrNull() ?: 0,
                                    dayOfWeek = fields[2].toIntOrNull() ?: 1,
                                    startTimeMinutes = fields[3].toIntOrNull() ?: 540,
                                    units = fields[4].toIntOrNull() ?: 1,
                                ))
                            }
                        }
                        "Attendance" -> {
                            if (fields.size >= 7) {
                                attendance.add(AttendanceEntity(
                                    id = fields[0].toLongOrNull() ?: 0,
                                    subjectId = fields[1].toLongOrNull() ?: 0,
                                    date = dateFmt.parse(fields[2])?.time ?: 0L,
                                    slotId = fields[3].toLongOrNull(),
                                    status = fields[4],
                                    units = fields[5].toIntOrNull() ?: 1,
                                    isAuto = fields[6].equals("Yes", ignoreCase = true),
                                ))
                            }
                        }
                    }
                }

                // Clear existing data and import
                repository.clearAllData()

                // Import in order (sessions → subjects → slots → attendance)
                val sessionIdMap = mutableMapOf<Long, Long>()
                sessions.forEach { s ->
                    val newId = repository.importSession(s.copy(id = 0))
                    sessionIdMap[s.id] = newId
                }

                val subjectIdMap = mutableMapOf<Long, Long>()
                subjects.forEach { s ->
                    val mappedSessionId = sessionIdMap[s.sessionId] ?: s.sessionId
                    val newId = repository.importSubject(s.copy(id = 0, sessionId = mappedSessionId))
                    subjectIdMap[s.id] = newId
                }

                slots.forEach { slot ->
                    val mappedSubjectId = subjectIdMap[slot.subjectId] ?: slot.subjectId
                    repository.importSlot(slot.copy(id = 0, subjectId = mappedSubjectId))
                }

                attendance.forEach { a ->
                    val mappedSubjectId = subjectIdMap[a.subjectId] ?: a.subjectId
                    repository.importAttendance(a.copy(id = 0, subjectId = mappedSubjectId))
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun parseCsvRow(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' && !inQuotes -> inQuotes = true
                c == '"' && inQuotes -> {
                    if (i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++
                    } else {
                        inQuotes = false
                    }
                }
                c == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current = StringBuilder()
                }
                else -> current.append(c)
            }
            i++
        }
        result.add(current.toString())
        return result
    }
}
