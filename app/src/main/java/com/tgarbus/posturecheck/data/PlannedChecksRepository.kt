package com.tgarbus.posturecheck.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.plannedChecksDataStore: DataStore<Preferences> by preferencesDataStore("PlannedChecks")
class PlannedChecksRepository(private val context: Context) {
  private val idsKey: Preferences.Key<Set<String>> = stringSetPreferencesKey("planned_posture_checks_ids")
  private val millisKey: (String) -> Preferences.Key<Long> = { pccId -> longPreferencesKey("${pccId}_millis") }

  private fun getPlannedPostureCheckById(id: String, preferences: Preferences): PlannedPostureCheck {
    return PlannedPostureCheck(
      id = id,
      millis = preferences[millisKey(id)]!!,
    )
  }

  fun getPlannedChecksAsFlow(): Flow<Set<PlannedPostureCheck>> {
    return context.plannedChecksDataStore.data.map { preferences ->
      val plannedChecks = HashSet<PlannedPostureCheck>()
      val allIds: Set<String> = preferences[idsKey] ?: HashSet()
      for (id in allIds) {
        plannedChecks.add(getPlannedPostureCheckById(id, preferences))
      }
      Log.i("tomek", plannedChecks.toString())
      plannedChecks
    }
  }

  suspend fun getPlannedChecks(): Set<PlannedPostureCheck> {
    return getPlannedChecksAsFlow().first()
  }

  private fun addIdToList(id: String, preferences: MutablePreferences) {
    val ids: HashSet<String> = preferences[idsKey]?.let { HashSet(it) } ?: HashSet()
    ids.add(id)
    preferences[idsKey] = ids
  }

  private fun removeIdFromList(id: String, preferences: MutablePreferences) {
    val ids: HashSet<String> = preferences[idsKey]?.let { HashSet(it) } ?: HashSet()
    ids.remove(id)
    preferences[idsKey] = ids
  }

  suspend fun addPlannedCheck(plannedPostureCheck: PlannedPostureCheck) {
    context.plannedChecksDataStore.edit { preferences ->
      preferences[millisKey(plannedPostureCheck.id)] = plannedPostureCheck.millis
      addIdToList(plannedPostureCheck.id, preferences)
    }
  }

  suspend fun deletePlannedCheck(plannedPostureCheck: PlannedPostureCheck) {
    context.plannedChecksDataStore.edit { preferences ->
      removeIdFromList(plannedPostureCheck.id, preferences)
    }
  }
}