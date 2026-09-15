package com.aditya.present.domain

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    AMOLED,
    CUSTOM;

    companion object {
        fun fromOrdinalSafe(value: Int): ThemeMode =
            entries.getOrElse(value) { SYSTEM }
    }
}
