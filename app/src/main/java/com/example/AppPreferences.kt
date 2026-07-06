package com.example

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class AppPreferences(private val context: Context) {
    companion object {
        val API_KEY = stringPreferencesKey("api_key")
        val VIDEO_INTERVAL = intPreferencesKey("video_interval")
        val DETAIL_LEVEL = stringPreferencesKey("detail_level")
        val SPEECH_RATE = floatPreferencesKey("speech_rate")
        val TEXT_SIZE = intPreferencesKey("text_size")
    }

    val apiKeyFlow: Flow<String?> = context.dataStore.data.map { it[API_KEY] }
    val videoIntervalFlow: Flow<Int> = context.dataStore.data.map { it[VIDEO_INTERVAL] ?: 2 }
    val detailLevelFlow: Flow<String> = context.dataStore.data.map { it[DETAIL_LEVEL] ?: "brief" }
    val speechRateFlow: Flow<Float> = context.dataStore.data.map { it[SPEECH_RATE] ?: 1.0f }
    val textSizeFlow: Flow<Int> = context.dataStore.data.map { it[TEXT_SIZE] ?: 24 }

    suspend fun saveApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[API_KEY] = apiKey
        }
    }
    
    suspend fun saveVideoInterval(interval: Int) {
        context.dataStore.edit { preferences ->
            preferences[VIDEO_INTERVAL] = interval
        }
    }
    
    suspend fun saveDetailLevel(level: String) {
        context.dataStore.edit { preferences ->
            preferences[DETAIL_LEVEL] = level
        }
    }

    suspend fun saveSpeechRate(rate: Float) {
        context.dataStore.edit { preferences ->
            preferences[SPEECH_RATE] = rate
        }
    }

    suspend fun saveTextSize(size: Int) {
        context.dataStore.edit { preferences ->
            preferences[TEXT_SIZE] = size
        }
    }
}
