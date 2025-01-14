package com.tgarbus.posturecheck

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.tgarbus.posturecheck.data.PastPostureCheck
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
    if (bundle.getBoolean("is_test_notification", false)) {
      scope.launch {
        dismissTestNotification(context)
        // TODO: Document why this is needed.
        stopSelf(startId)
      }
      return START_REDELIVER_INTENT
    }
    val pastCheck: PastPostureCheck = PastPostureCheck.fromBundle(bundle)
    scope.launch {
      storeReplyAndCancelNotification(context, pastCheck)
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