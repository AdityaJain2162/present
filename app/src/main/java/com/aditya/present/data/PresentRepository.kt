package com.aditya.present.data

import com.aditya.present.domain.AttendanceStatus
import com.aditya.present.domain.SessionType
import kotlinx.coroutines.flow.Flow
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
