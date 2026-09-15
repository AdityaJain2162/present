package com.aditya.present.domain

enum class SessionType {
    SEMESTER,
    YEARLY;

    companion object {
        fun fromOrdinalSafe(value: Int): SessionType =
            entries.getOrElse(value) { SEMESTER }
    }
}
