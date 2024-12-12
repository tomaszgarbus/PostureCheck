package com.tgarbus.posturecheck.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.PreferencesProto
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.pastChecksDataStore: DataStore<Preferences> by preferencesDataStore("PastChecksHistory")
class PastChecksRepository(private val context: Context) {
  private val idsKey: Preferences.Key<Set<String>> = stringSetPreferencesKey("past_posture_checks_ids")
  private val millisKey: (String) -> Preferences.Key<Long> = { pccId -> longPreferencesKey("${pccId}_millis") }
  private val replyKey: (String) -> Preferences.Key<String> = { pccId -> stringPreferencesKey("${pccId}_reply") }

  private fun getPastPostureCheckById(id: String, preferences: Preferences): PastPostureCheck {
    return PastPostureCheck(
      PlannedPostureCheck(
        id = id,
        millis = preferences[millisKey(id)]!!
      ),
      reply = PostureCheckReply.valueOf(preferences[replyKey(id)]!!),
    )
  }

  fun getPastChecksHistoryAsFlow(): Flow<Set<PastPostureCheck>> {
    return context.pastChecksDataStore.data.map { preferences ->
      val pastChecks = HashSet<PastPostureCheck>()
      val allIds: Set<String> = preferences[idsKey] ?: HashSet()
      for (id in allIds) {
        pastChecks.add(getPastPostureCheckById(id, preferences))
      }
      Log.i("tomek", pastChecks.toString())
      pastChecks
    }
  }

  private fun addIdToList(id: String, preferences: MutablePreferences) {
    val ids: HashSet<String> = preferences[idsKey]?.let { HashSet(it) } ?: HashSet()
    ids.add(id)
    preferences[idsKey] = ids
  }

  private fun addIdToList(ids: List<String>, preferences: MutablePreferences) {
    val allIds: HashSet<String> = preferences[idsKey]?.let { HashSet(it) } ?: HashSet()
    for (id in ids) {
      allIds.add(id)
    }
    preferences[idsKey] = allIds
  }

  suspend fun addPastChecks(checks: ArrayList<PastPostureCheck>) {
    context.pastChecksDataStore.edit { preferences ->
      for (check in checks) {
        preferences[millisKey(check.planned.id)] = check.planned.millis
        preferences[replyKey(check.planned.id)] = check.reply.name
      }
      addIdToList(checks.map { it.planned.id }, preferences)
      Log.i("tomek", preferences.toString())
    }
  }

  suspend fun addPastCheck(pastPostureCheck: PastPostureCheck) {
    context.pastChecksDataStore.edit { preferences ->
      preferences[millisKey(pastPostureCheck.planned.id)] = pastPostureCheck.planned.millis
      preferences[replyKey(pastPostureCheck.planned.id)] = pastPostureCheck.reply.name
      addIdToList(pastPostureCheck.planned.id, preferences)
      Log.i("tomek", preferences.toString())
    }
  }
}