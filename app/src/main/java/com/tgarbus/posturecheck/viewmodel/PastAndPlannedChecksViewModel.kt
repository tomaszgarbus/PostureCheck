package com.tgarbus.posturecheck.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tgarbus.posturecheck.data.PastChecksRepository
import com.tgarbus.posturecheck.data.PastPostureCheck
import kotlinx.coroutines.launch

class PastAndPlannedChecksViewModel : ViewModel() {
  fun savePastPostureCheck(pastPostureCheck: PastPostureCheck, context: Context) {
    viewModelScope.launch {
      PastChecksRepository(context).addPastCheck(pastPostureCheck)
    }
  }
}