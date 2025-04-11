package com.tgarbus.posturecheck.data

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class NotificationsViewModel(): ViewModel() {
    fun getPastChecks(context: Context): Flow<Set<PastPostureCheck>> {
        return PastChecksRepository(context).getPastChecksHistoryAsFlow()
    }

    fun updatePastCheck(
        context: Context, pastPostureCheck: PastPostureCheck, reply: PostureCheckReply) {
        viewModelScope.launch {
            PastChecksRepository(context).updateResponse(pastPostureCheck, reply)
        }
    }
}