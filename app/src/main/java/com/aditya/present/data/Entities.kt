package com.aditya.present.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "academic_sessions")
data class AcademicSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String,
    val startDate: Long,
    val endDate: Long,
    val targetAttendancePercent: Float = 75f,
    val isActive: Boolean = true,
)

@Entity(
    tableName = "subjects",
    foreignKeys = [
        ForeignKey(
            entity = AcademicSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("sessionId")]
)
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val name: String,
    val acronym: String,
    val color: Int,
    val targetAttendancePercent: Float = 75f,
    val totalUnits: Int = 1,
    val teacherName: String = "",
)

@Entity(
    tableName = "class_slots",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("subjectId")]
)
data class ClassSlotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val dayOfWeek: Int,
    val startTimeMinutes: Int,
    val units: Int = 1,
)

@Entity(
    tableName = "attendance",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("subjectId"), Index("date")]
)
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val date: Long,
    val slotId: Long? = null,
    val status: String,
    val units: Int = 1,
    val isAuto: Boolean = false,
)
