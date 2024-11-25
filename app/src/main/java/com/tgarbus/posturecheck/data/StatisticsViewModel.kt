package com.tgarbus.posturecheck.data

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class StatisticsViewModel(): ViewModel() {
    fun getPastPostureChecks(context: Context): Flow<Set<PastPostureCheck>> {
        return PastChecksRepository(context).getPastChecksHistoryAsFlow()
    }
}