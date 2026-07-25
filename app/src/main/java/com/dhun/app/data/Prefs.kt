package com.dhun.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "dhun_prefs")

class Prefs(private val ctx: Context) {

    // Keys
    companion object {
        val PERM_GRANTED = booleanPreferencesKey("perm_granted")
        val GAPLESS = booleanPreferencesKey("gapless")
        val CROSSFADE_SEC = intPreferencesKey("crossfade_sec") // 0 = off
        val PLAYBACK_SPEED = floatPreferencesKey("speed")
        val RESUME_ON_HEADPHONE = booleanPreferencesKey("resume_hp")
        val PAUSE_ON_DISCONNECT = booleanPreferencesKey("pause_disc")
        val AUDIO_FOCUS = stringPreferencesKey("audio_focus") // duck / pause
        val EQ_ENABLED = booleanPreferencesKey("eq_enabled")
        val EQ_PRESET = stringPreferencesKey("eq_preset")
        val BASS_BOOST = intPreferencesKey("bass")
        val VIRT = booleanPreferencesKey("virt")
        val NORMALIZE = booleanPreferencesKey("normalize")
        val THEME = stringPreferencesKey("theme") // dark/black/light
        val ACCENT = stringPreferencesKey("accent")
        val ART_STYLE = stringPreferencesKey("art_style")
        val DENSITY = stringPreferencesKey("density")
        val SLEEP_MINS = intPreferencesKey("sleep_mins")
        val MIN_DURATION_SEC = intPreferencesKey("min_dur_sec")
        val LAST_SCAN = longPreferencesKey("last_scan")
        val SCAN_FOLDERS = stringPreferencesKey("scan_folders") // comma separated, empty = all
        val IGNORE_FOLDERS = stringPreferencesKey("ignore_folders")
        val FORMATS = stringPreferencesKey("formats") // comma separated
    }

    fun <T> flow(key: androidx.datastore.preferences.core.Preferences.Key<T>, default: T): Flow<T> =
        ctx.dataStore.data.map { it[key] ?: default }

    suspend fun <T> set(key: androidx.datastore.preferences.core.Preferences.Key<T>, value: T) {
        ctx.dataStore.edit { it[key] = value }
    }
}
