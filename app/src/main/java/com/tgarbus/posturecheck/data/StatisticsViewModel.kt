package com.tgarbus.posturecheck.data

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow

class StatisticsViewModel(): ViewModel() {
    fun getPastPostureChecks(context: Context): Flow<Set<PastPostureCheck>> {
        return PastChecksRepository(context).getPastChecksHistoryAsFlow()
    }

    fun getPastPostureChecks(context: Context, day: Day): Flow<Set<PastPostureCheck>> {
        return PastChecksRepository(context).getChecksForDayAsFlow(day)
    }

    fun getPastPostureChecks(context: Context, days: Collection<Day>): Flow<Set<PastPostureCheck>> {
        return PastChecksRepository(context).getChecksForDaysAsFlow(days)
    }
}