package com.tgarbus.posturecheck.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.onboardingDataStore: DataStore<Preferences> by preferencesDataStore("Onboarding")
class OnboardingRepository(private val context: Context) {
    private val onboardingScreenCompletedKey: Preferences.Key<Boolean> = booleanPreferencesKey("onboarding_screen_completed")

    fun isIntroScreenCompleted(): Flow<Boolean> {
        return context.onboardingDataStore.data.map { preferences ->
            preferences[onboardingScreenCompletedKey] ?: false
        }
    }

    suspend fun markIntroScreenCompleted() {
        context.onboardingDataStore.edit { mutablePreferences ->
            mutablePreferences[onboardingScreenCompletedKey] = true
        }
    }
}