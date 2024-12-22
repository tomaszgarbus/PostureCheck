package com.tgarbus.posturecheck.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.pastChecksDataStore: DataStore<Preferences> by preferencesDataStore("PastChecksHistory")
class PastChecksRepository(private val context: Context) {
  private val idsPerDayKey: (Day) -> Preferences.Key<Set<String>> = { day -> stringSetPreferencesKey("past_posture_checks_ids_${day}") }
  private val daysWithEntriesKey: Preferences.Key<Set<String>> = stringSetPreferencesKey("days_with_past_posture_checks")
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

  private fun getDaysWithEntries(preferences: Preferences): Set<Day> {
    val strs = preferences[daysWithEntriesKey] ?: HashSet()
    return strs.map { str ->
      Day.parseString(str)
    }.toSet()
  }

  private fun getChecksForDay(day: Day, preferences: Preferences): Set<PastPostureCheck> {
    val idsForDay = preferences[idsPerDayKey(day)] ?: HashSet()
    Log.d("tomek", "Debuggging getChecksForDay: $day, $idsForDay")
    return idsForDay.map { id -> getPastPostureCheckById(id, preferences) }.toSet()
  }

  fun getChecksForDaysAsFlow(days: Collection<Day>): Flow<Set<PastPostureCheck>> {
    return context.pastChecksDataStore.data.map { preferences ->
      val result = HashSet<PastPostureCheck>()
      for (day in days) {
        val idsForDay = preferences[idsPerDayKey(day)] ?: HashSet()
        result.addAll(idsForDay.map { id -> getPastPostureCheckById(id, preferences) })
      }
      result
    }
  }

  fun getPastChecksHistoryAsFlow(): Flow<Set<PastPostureCheck>> {
    return context.pastChecksDataStore.data.map { preferences ->
      val allDays = getDaysWithEntries(preferences)
      val allChecks = HashSet<PastPostureCheck>()
      Log.d("tomek", "Debug preferences: $preferences")
      for (day in allDays) {
        allChecks.addAll(getChecksForDay(day, preferences))
      }
      Log.d("tomek", "Found checks: $allChecks")
      allChecks
    }
  }

  private fun addIdToList(id: String, day: Day, preferences: MutablePreferences) {
    // First, add id to list for that day.
    val idsPerDay = preferences[idsPerDayKey(day)]?.let { HashSet(it) } ?: HashSet()
    idsPerDay.add(id)
    preferences[idsPerDayKey(day)] = idsPerDay

    // Now, update the list of days with events.
    val daysWithEvents = preferences[daysWithEntriesKey]?.let { HashSet(it) } ?: HashSet()
    daysWithEvents.add(day.toString())
    preferences[daysWithEntriesKey] = daysWithEvents
  }

  suspend fun addPastCheck(pastPostureCheck: PastPostureCheck) {
    context.pastChecksDataStore.edit { preferences ->
      preferences[millisKey(pastPostureCheck.planned.id)] = pastPostureCheck.planned.millis
      preferences[replyKey(pastPostureCheck.planned.id)] = pastPostureCheck.reply.name
      addIdToList(pastPostureCheck.planned.id, pastPostureCheck.planned.getDay(), preferences)
    }
  }
}