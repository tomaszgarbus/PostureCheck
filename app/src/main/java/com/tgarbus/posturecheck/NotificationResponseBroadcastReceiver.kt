package com.tgarbus.posturecheck

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.tgarbus.posturecheck.data.PastChecksRepository
import com.tgarbus.posturecheck.data.PastPostureCheck
import com.tgarbus.posturecheck.data.PlannedChecksRepository

class NotificationResponseBroadcastReceiver: BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    val plannedChecksRepository = PlannedChecksRepository(context)
    val pastChecksRepository = PastChecksRepository(context)

    val pastPostureCheck = PastPostureCheck.fromBundle(intent.extras!!)
    
  }

}