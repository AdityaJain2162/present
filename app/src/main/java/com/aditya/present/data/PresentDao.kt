package com.aditya.present.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PresentDao {

    // Academic sessions
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: AcademicSessionEntity): Long

    @Update
    suspend fun updateSession(session: AcademicSessionEntity)

    @Query("SELECT * FROM academic_sessions ORDER BY startDate DESC")
    fun getAllSessions(): Flow<List<AcademicSessionEntity>>

    @Query("SELECT * FROM academic_sessions WHERE isActive = 1 LIMIT 1")
    fun getActiveSession(): Flow<AcademicSessionEntity?>

    @Query("UPDATE academic_sessions SET isActive = (id = :activeId)")
    suspend fun setActiveSession(activeId: Long)

    // Subjects
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity): Long

    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    @Delete
    suspend fun deleteSubject(subject: SubjectEntity)

    @Query("SELECT * FROM subjects WHERE sessionId = :sessionId ORDER BY name")
    fun getSubjectsForSession(sessionId: Long): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getSubjectById(id: Long): SubjectEntity?

    // Class slots
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlot(slot: ClassSlotEntity): Long

    @Query("SELECT * FROM class_slots WHERE subjectId = :subjectId ORDER BY dayOfWeek, startTimeMinutes")
    fun getSlotsForSubject(subjectId: Long): Flow<List<ClassSlotEntity>>

    @Query("SELECT * FROM class_slots WHERE subjectId = :subjectId AND dayOfWeek = :dayOfWeek ORDER BY startTimeMinutes")
    suspend fun getSlotsForSubjectOnDay(subjectId: Long, dayOfWeek: Int): List<ClassSlotEntity>

    @Query("SELECT * FROM class_slots WHERE dayOfWeek = :dayOfWeek ORDER BY startTimeMinutes")
    fun getSlotsForDay(dayOfWeek: Int): Flow<List<ClassSlotEntity>>

    // Attendance
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(entry: AttendanceEntity): Long

    @Update
    suspend fun updateAttendance(entry: AttendanceEntity)

    @Delete
    suspend fun deleteAttendance(entry: AttendanceEntity)

    @Query("SELECT * FROM attendance WHERE subjectId = :subjectId ORDER BY date DESC")
    fun getAttendanceForSubject(subjectId: Long): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE date BETWEEN :startOfDay AND :endOfDay ORDER BY date")
    fun getAttendanceForDate(startOfDay: Long, endOfDay: Long): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE subjectId = :subjectId AND date BETWEEN :start AND :end ORDER BY date")
    fun getAttendanceForSubjectInRange(subjectId: Long, start: Long, end: Long): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE id = :id")
    suspend fun getAttendanceById(id: Long): AttendanceEntity?

    @Query("DELETE FROM attendance WHERE id = :id")
    suspend fun deleteAttendanceById(id: Long)

    @Query("SELECT * FROM attendance WHERE subjectId = :subjectId AND date BETWEEN :start AND :end LIMIT 1")
    suspend fun getAttendanceForSubjectOnDate(subjectId: Long, start: Long, end: Long): AttendanceEntity?

    @Query("SELECT * FROM attendance WHERE subjectId = :subjectId AND slotId = :slotId AND date BETWEEN :start AND :end LIMIT 1")
    suspend fun getAttendanceForSlotOnDate(subjectId: Long, slotId: Long, start: Long, end: Long): AttendanceEntity?

    @Query("SELECT * FROM attendance WHERE subjectId = :subjectId AND slotId IS NULL AND date BETWEEN :start AND :end LIMIT 1")
    suspend fun getSlotlessAttendanceForSubjectOnDate(subjectId: Long, start: Long, end: Long): AttendanceEntity?

    @Query("DELETE FROM class_slots WHERE id = :slotId")
    suspend fun deleteSlot(slotId: Long)

    @Query("SELECT * FROM subjects")
    suspend fun getAllSubjects(): List<SubjectEntity>

    @Query("SELECT * FROM class_slots")
    suspend fun getAllSlots(): List<ClassSlotEntity>

    @Query("SELECT * FROM attendance")
    suspend fun getAllAttendance(): List<AttendanceEntity>
}
