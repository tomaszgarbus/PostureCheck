package com.tgarbus.posturecheck.data

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class OnboardingViewModel(): ViewModel() {
    fun markOnboardingCompleted(context: Context) {
        viewModelScope.launch {
            OnboardingRepository(context).markIntroScreenCompleted()
        }
    }

    fun isOnboardingCompleted(context: Context): Flow<Boolean> {
        return OnboardingRepository(context).isIntroScreenCompleted()
    }
}