package com.tgarbus.posturecheck.data

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow

class NotificationsViewModel(): ViewModel() {
    fun getPastChecks(context: Context): Flow<Set<PastPostureCheck>> {
        return PastChecksRepository(context).getPastChecksHistoryAsFlow()
    }
}