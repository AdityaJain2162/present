package com.aditya.present.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aditya.present.domain.ThemeMode
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "present_prefs")

data class ThemePrefs(
    val mode: ThemeMode = ThemeMode.SYSTEM,
    val accentName: String = "Teal",
    val dynamicColor: Boolean = true,
    val animationsEnabled: Boolean = true,
    val hapticFeedback: Boolean = true,
    val defaultTargetPercent: Int = 75,
    val weekendDays: String = "SATURDAY,SUNDAY",
    val autoMarkHour: Int = 22,
    val cardStyle: String = "filled",
    val showPercentageOnCards: Boolean = true,
    val compactMode: Boolean = false,
    val hapticIntensity: String = "MEDIUM", // LOW, MEDIUM, HIGH
    val classNotificationsEnabled: Boolean = true,
    val notificationLeadMinutes: Int = 10,
    val autoMarkEnabled: Boolean = false,
)

@Singleton
class ThemeRepository @Inject constructor(
    private val context: Context,
) {
    private val modeKey = intPreferencesKey("theme_mode")
    private val accentKey = stringPreferencesKey("accent_name")
    private val dynamicKey = booleanPreferencesKey("dynamic_color")
    private val animKey = booleanPreferencesKey("animations_enabled")
    private val hapticKey = booleanPreferencesKey("haptic_feedback")
    private val targetKey = intPreferencesKey("default_target_percent")
    private val weekendKey = stringPreferencesKey("weekend_days")
    private val autoMarkKey = intPreferencesKey("auto_mark_hour")
    private val cardStyleKey = stringPreferencesKey("card_style")
    private val showPctKey = booleanPreferencesKey("show_pct_on_cards")
    private val compactKey = booleanPreferencesKey("compact_mode")
    private val hapticIntensityKey = stringPreferencesKey("haptic_intensity")
    private val classNotifKey = booleanPreferencesKey("class_notifications_enabled")
    private val notifLeadKey = intPreferencesKey("notification_lead_minutes")
    private val autoMarkEnabledKey = booleanPreferencesKey("auto_mark_enabled")

    val themePrefs: Flow<ThemePrefs> = context.dataStore.data.map { prefs ->
        ThemePrefs(
            mode = ThemeMode.fromOrdinalSafe(prefs[modeKey] ?: 0),
            accentName = prefs[accentKey] ?: "Teal",
            dynamicColor = prefs[dynamicKey] ?: true,
            animationsEnabled = prefs[animKey] ?: true,
            hapticFeedback = prefs[hapticKey] ?: true,
            defaultTargetPercent = prefs[targetKey] ?: 75,
            weekendDays = prefs[weekendKey] ?: "SATURDAY,SUNDAY",
            autoMarkHour = prefs[autoMarkKey] ?: 22,
            cardStyle = prefs[cardStyleKey] ?: "filled",
            showPercentageOnCards = prefs[showPctKey] ?: true,
            compactMode = prefs[compactKey] ?: false,
            hapticIntensity = prefs[hapticIntensityKey] ?: "MEDIUM",
            classNotificationsEnabled = prefs[classNotifKey] ?: true,
            notificationLeadMinutes = prefs[notifLeadKey] ?: 10,
            autoMarkEnabled = prefs[autoMarkEnabledKey] ?: false,
        )
    }

    val themeMode: Flow<ThemeMode> = themePrefs.map { it.mode }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[modeKey] = mode.ordinal }
    }

    suspend fun setAccentName(name: String) {
        context.dataStore.edit { it[accentKey] = name }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[dynamicKey] = enabled }
    }

    suspend fun setAnimationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[animKey] = enabled }
    }

    suspend fun setHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { it[hapticKey] = enabled }
    }

    suspend fun setDefaultTargetPercent(percent: Int) {
        context.dataStore.edit { it[targetKey] = percent.coerceIn(50, 100) }
    }

    suspend fun setWeekendDays(days: String) {
        context.dataStore.edit { it[weekendKey] = days }
    }

    suspend fun setAutoMarkHour(hour: Int) {
        context.dataStore.edit { it[autoMarkKey] = hour.coerceIn(0, 23) }
    }

    suspend fun setCardStyle(style: String) {
        context.dataStore.edit { it[cardStyleKey] = style }
    }

    suspend fun setShowPercentageOnCards(enabled: Boolean) {
        context.dataStore.edit { it[showPctKey] = enabled }
    }

    suspend fun setCompactMode(enabled: Boolean) {
        context.dataStore.edit { it[compactKey] = enabled }
    }

    suspend fun setHapticIntensity(intensity: String) {
        context.dataStore.edit { it[hapticIntensityKey] = intensity }
    }

    suspend fun setClassNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[classNotifKey] = enabled }
    }

    suspend fun setNotificationLeadMinutes(minutes: Int) {
        context.dataStore.edit { it[notifLeadKey] = minutes.coerceIn(0, 60) }
    }

    suspend fun setAutoMarkEnabled(enabled: Boolean) {
        context.dataStore.edit { it[autoMarkEnabledKey] = enabled }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {

    @Provides
    @Singleton
    fun provideThemeRepository(@ApplicationContext context: Context): ThemeRepository =
        ThemeRepository(context)
}
