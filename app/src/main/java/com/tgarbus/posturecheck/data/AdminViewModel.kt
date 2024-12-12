package com.tgarbus.posturecheck.data

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AdminViewModel(): ViewModel() {
    fun addPastChecks(context: Context, checks: ArrayList<PastPostureCheck>, onFinish: () -> Unit) {
        val repo = PastChecksRepository(context)
        viewModelScope.launch {
            for (check in checks) {
                repo.addPastCheck(check)
            }
            onFinish()
        }
    }
}