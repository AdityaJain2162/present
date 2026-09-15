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
)

@Singleton
class ThemeRepository @Inject constructor(
    private val context: Context,
) {
    private val modeKey = intPreferencesKey("theme_mode")
    private val accentKey = stringPreferencesKey("accent_name")
    private val dynamicKey = booleanPreferencesKey("dynamic_color")
    private val animKey = booleanPreferencesKey("animations_enabled")

    val themePrefs: Flow<ThemePrefs> = context.dataStore.data.map { prefs ->
        ThemePrefs(
            mode = ThemeMode.fromOrdinalSafe(prefs[modeKey] ?: 0),
            accentName = prefs[accentKey] ?: "Teal",
            dynamicColor = prefs[dynamicKey] ?: true,
            animationsEnabled = prefs[animKey] ?: true,
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
}

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {

    @Provides
    @Singleton
    fun provideThemeRepository(@ApplicationContext context: Context): ThemeRepository =
        ThemeRepository(context)
}
