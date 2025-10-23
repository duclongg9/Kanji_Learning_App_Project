package com.example.kanjilearning.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * VI: DataStore lưu các flag nhẹ như đã chạy onboarding hay chưa.
 */
@Singleton
class UserPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    private object Keys {
        val onboardingFinished = booleanPreferencesKey("onboarding_finished")
        val lastSrsReminder = longPreferencesKey("last_srs_reminder")
    }

    /**
     * VI: Quan sát trạng thái onboarding.
     */
    val onboardingFinished: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.onboardingFinished] ?: false
    }

    /**
     * VI: Lưu trạng thái đã hoàn thành onboarding.
     */
    suspend fun setOnboardingFinished(value: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.onboardingFinished] = value
        }
    }

    /**
     * VI: Lưu timestamp nhắc SRS lần gần nhất để tránh spam.
     */
    suspend fun updateLastReminder(timestamp: Long) {
        dataStore.edit { prefs ->
            prefs[Keys.lastSrsReminder] = timestamp
        }
    }

    /**
     * VI: Đọc timestamp nhắc SRS gần nhất.
     */
    val lastReminder: Flow<Long> = dataStore.data.map { prefs ->
        prefs[Keys.lastSrsReminder] ?: 0L
    }
}
