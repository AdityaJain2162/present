package com.aditya.present.data

import com.aditya.present.domain.AttendanceStatus
import com.aditya.present.domain.SessionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class SubjectWithStats(
    val subject: SubjectEntity,
    val attendedUnits: Int,
    val totalUnits: Int,
    val percentage: Float,
)

@Singleton
class PresentRepository @Inject constructor(
    private val dao: PresentDao,
) {
    // Sessions
    fun getAllSessions(): Flow<List<AcademicSessionEntity>> = dao.getAllSessions()
    fun getActiveSession(): Flow<AcademicSessionEntity?> = dao.getActiveSession()

    suspend fun insertSession(
        name: String,
        type: SessionType,
        startDate: Long,
        endDate: Long,
        target: Float,
    ): Long {
        dao.setActiveSession(0) // deactivate all first (no-op if none)
        return dao.insertSession(
            AcademicSessionEntity(
                name = name,
                type = type.name,
                startDate = startDate,
                endDate = endDate,
                targetAttendancePercent = target,
                isActive = true,
            )
        ).also { newId ->
            dao.setActiveSession(newId)
        }
    }

    suspend fun setActiveSession(id: Long) = dao.setActiveSession(id)

    // Subjects
    fun getSubjectsForSession(sessionId: Long): Flow<List<SubjectEntity>> =
        dao.getSubjectsForSession(sessionId)

    suspend fun getSubjectById(id: Long): SubjectEntity? = dao.getSubjectById(id)

    suspend fun insertSubject(
        sessionId: Long,
        name: String,
        acronym: String,
        color: Int,
        target: Float,
        totalUnits: Int,
        teacherName: String = "",
    ): Long = dao.insertSubject(
        SubjectEntity(
            sessionId = sessionId,
            name = name,
            acronym = acronym,
            color = color,
            targetAttendancePercent = target,
            totalUnits = totalUnits,
            teacherName = teacherName,
        )
    )

    suspend fun updateSubject(subject: SubjectEntity) = dao.updateSubject(subject)
    suspend fun deleteSubject(subject: SubjectEntity) = dao.deleteSubject(subject)

    // Class slots
    fun getSlotsForDay(dayOfWeek: Int): Flow<List<ClassSlotEntity>> = dao.getSlotsForDay(dayOfWeek)

    suspend fun insertSlot(subjectId: Long, dayOfWeek: Int, startTimeMinutes: Int, units: Int): Long =
        dao.insertSlot(ClassSlotEntity(subjectId = subjectId, dayOfWeek = dayOfWeek, startTimeMinutes = startTimeMinutes, units = units))

    suspend fun deleteSlot(slotId: Long) = dao.deleteSlot(slotId)

    suspend fun getSlotsForSubjectOnDay(subjectId: Long, dayOfWeek: Int): List<ClassSlotEntity> =
        dao.getSlotsForSubjectOnDay(subjectId, dayOfWeek)

    // Export helpers
    suspend fun getAllSubjects(): List<SubjectEntity> = dao.getAllSubjects()
    suspend fun getAllSlots(): List<ClassSlotEntity> = dao.getAllSlots()
    suspend fun getAllAttendance(): List<AttendanceEntity> = dao.getAllAttendance()
    suspend fun getAllSessionsList(): List<AcademicSessionEntity> = dao.getAllSessions().first()

    suspend fun clearAllData() {
        dao.clearAttendance()
        dao.clearSlots()
        dao.clearSubjects()
        dao.clearSessions()
    }

    suspend fun importSession(session: AcademicSessionEntity): Long = dao.insertSession(session)
    suspend fun importSubject(subject: SubjectEntity): Long = dao.insertSubject(subject)
    suspend fun importSlot(slot: ClassSlotEntity): Long = dao.insertSlot(slot)
    suspend fun importAttendance(entry: AttendanceEntity): Long = dao.insertAttendance(entry)

    // Attendance
    fun getAttendanceForSubject(subjectId: Long): Flow<List<AttendanceEntity>> =
        dao.getAttendanceForSubject(subjectId)

    fun getAttendanceForDate(startOfDay: Long, endOfDay: Long): Flow<List<AttendanceEntity>> =
        dao.getAttendanceForDate(startOfDay, endOfDay)

    suspend fun insertAttendance(
        subjectId: Long,
        date: Long,
        status: AttendanceStatus,
        units: Int = 1,
        slotId: Long? = null,
        isAuto: Boolean = false,
    ): Long = dao.insertAttendance(
        AttendanceEntity(
            subjectId = subjectId,
            date = date,
            slotId = slotId,
            status = status.name,
            units = units,
            isAuto = isAuto,
        )
    )

    suspend fun updateAttendance(entry: AttendanceEntity) = dao.updateAttendance(entry)
    suspend fun deleteAttendance(entry: AttendanceEntity) = dao.deleteAttendance(entry)
    suspend fun deleteAttendanceById(id: Long) = dao.deleteAttendanceById(id)

    suspend fun updateAttendanceStatus(id: Long, status: AttendanceStatus) =
        dao.updateAttendanceStatus(id, status.name)

    suspend fun getAttendanceForDateList(start: Long, end: Long): List<AttendanceEntity> =
        dao.getAttendanceForDateList(start, end)

    /**
     * Upsert attendance: if an entry exists for this subject+slot on this date,
     * update it; otherwise insert a new one. Slot-aware: if slotId is provided,
     * checks by subjectId+slotId+date; if null, checks by subjectId+date (slotless).
     * This allows multiple entries per subject per day when linked to different slots.
     */
    suspend fun upsertAttendance(
        subjectId: Long,
        date: Long,
        status: AttendanceStatus,
        units: Int = 1,
        slotId: Long? = null,
        isAuto: Boolean = false,
    ): Long {
        val startOfDay = startOfDay(date)
        val endOfDay = endOfDay(date)
        val existing = if (slotId != null) {
            dao.getAttendanceForSlotOnDate(subjectId, slotId, startOfDay, endOfDay)
        } else {
            dao.getSlotlessAttendanceForSubjectOnDate(subjectId, startOfDay, endOfDay)
        }
        return if (existing != null) {
            val updated = existing.copy(status = status.name, units = units, slotId = slotId, isAuto = isAuto)
            dao.updateAttendance(updated)
            existing.id
        } else {
            dao.insertAttendance(
                AttendanceEntity(
                    subjectId = subjectId,
                    date = date,
                    slotId = slotId,
                    status = status.name,
                    units = units,
                    isAuto = isAuto,
                )
            )
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

    // Stats
    fun getSubjectsWithStats(sessionId: Long): Flow<List<SubjectWithStats>> =
        dao.getSubjectsForSession(sessionId).map { subjects ->
            subjects.map { subject ->
                val entries = dao.getAttendanceForSubject(subject.id)
                // Simplified: will be expanded with Flow composition
                SubjectWithStats(
                    subject = subject,
                    attendedUnits = 0,
                    totalUnits = 0,
                    percentage = 0f,
                )
            }
        }
}
