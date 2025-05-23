package com.tgarbus.posturecheck.data

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow

class StatisticsViewModel(): ViewModel() {
    fun getPostureChecksFromToday(context: Context): Flow<Set<PastPostureCheck>> {
        return PastChecksRepository(context).getChecksForDaysAsFlow(listOf(Day.today()))
    }

    fun getPastPostureChecks(context: Context, periodType: PeriodType, includeToday: Boolean): Flow<Set<PastPostureCheck>> {
        return when (periodType) {
            PeriodType.WEEK -> PastChecksRepository(context).getChecksForDaysAsFlow(lastWeek(includeToday))
            PeriodType.MONTH -> PastChecksRepository(context).getChecksForDaysAsFlow(lastMonth(includeToday))
            PeriodType.ALL_TIME -> PastChecksRepository(context).getPastChecksHistoryAsFlow()
        }
    }

    fun getMinTimeOfDay(context: Context): Flow<TimeOfDay> {
        return SettingsRepository(context).getEarliestNotificationTimeAsFlow()
    }

    fun getMaxTimeOfDay(context: Context): Flow<TimeOfDay> {
        return SettingsRepository(context).getLatestNotificationTimeAsFlow()
    }
}