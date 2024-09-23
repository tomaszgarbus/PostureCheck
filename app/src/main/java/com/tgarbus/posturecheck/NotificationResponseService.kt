package com.tgarbus.posturecheck

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import com.tgarbus.posturecheck.data.PastChecksRepository
import com.tgarbus.posturecheck.data.PastPostureCheck
import com.tgarbus.posturecheck.data.PlannedChecksRepository
import com.tgarbus.posturecheck.data.PlannedPostureCheck
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class NotificationResponseService : Service() {
  private val job = SupervisorJob()
  private val scope = CoroutineScope(job)

  override fun onCreate() {
    super.onCreate()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    Log.i("tomek", "NotificationResponseService::onStartCommand")
    val result = super.onStartCommand(intent, flags, startId)
    val context = this
    val pastCheck: PastPostureCheck = PastPostureCheck.fromBundle(intent!!.extras!!)
    scope.launch {
      val plannedChecksRepo = PlannedChecksRepository(context)
      val pastChecksRepo = PastChecksRepository(context)
      val plannedCheck = pastCheck.withoutReply()
      pastChecksRepo.addPastCheck(pastCheck)
      plannedChecksRepo.deletePlannedCheck(plannedCheck)
      Log.i("tomek", "Recorded response")
    }
    return result
  }

  override fun onBind(intent: Intent): IBinder? {
    Log.i("tomek", "NotificationResponseService::onBind")
    return null
  }

  override fun onDestroy() {
    Log.i("tomek", "NotificationResponseService::onDestroy")
    super.onDestroy()
    // TODO: implement fault recovery for `job` or try to block until it finishes.
    job.cancel()
  }
}