package com.tgarbus.posturecheck

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.tgarbus.posturecheck.data.PastChecksRepository
import com.tgarbus.posturecheck.data.PastPostureCheck
import com.tgarbus.posturecheck.data.PlannedChecksRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


class NotificationResponseService : Service() {
  private val job = SupervisorJob()
  private val scope = CoroutineScope(job)

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    super.onStartCommand(intent, flags, startId)
    val context = this
    val bundle = intent!!.extras!!
    val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    if (bundle.getBoolean("is_test_notification", false)) {
      notificationManager.cancel(kTestNotificationId)
      // TODO: Document why this is needed.
      stopSelf(startId)
      return START_REDELIVER_INTENT
    }
    val pastCheck: PastPostureCheck = PastPostureCheck.fromBundle(bundle)
    scope.launch {
      val plannedChecksRepo = PlannedChecksRepository(context)
      val pastChecksRepo = PastChecksRepository(context)
      val plannedCheck = pastCheck.withoutReply()
      pastChecksRepo.addPastCheck(pastCheck)
      plannedChecksRepo.deletePlannedCheck(plannedCheck)
      notificationManager.cancel(pastCheck.notificationId())
      // TODO: Document why this is needed.
      stopSelf(startId)
    }
    return START_REDELIVER_INTENT
  }

  override fun onBind(intent: Intent): IBinder? {
    return null
  }

  override fun onDestroy() {
    super.onDestroy()
    // TODO: implement fault recovery for `job` or try to block until it finishes.
    job.cancel()
  }
}