package com.tgarbus.posturecheck

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.tgarbus.posturecheck.data.LatestNotificationTimestampRepository
import com.tgarbus.posturecheck.data.PlannedPostureCheck
import com.tgarbus.posturecheck.data.PostureCheckReply
import kotlinx.coroutines.runBlocking

class NotificationAlarmBroadcastReceiver : BroadcastReceiver() {

  private fun buildPendingIntentForReply(
    reply: PostureCheckReply, plannedPostureCheck: PlannedPostureCheck,
    context: Context): PendingIntent {
    val baseIntent = Intent(context, NotificationResponseService::class.java)
    return baseIntent.let { intent ->
      intent.putExtras(plannedPostureCheck.withReply(reply).toBundle())
      // TODO: Rewrite construction of request code to make it clear there can be no collision.
      val requestCode = plannedPostureCheck.notificationId() + reply.ordinal
      // TODO: Comment why FLAG_UPDATE_CURRENT and FLAG_MUTABLE are needed.
      PendingIntent.getService(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
    }
  }

  override fun onReceive(context: Context, intent: Intent) {
    val plannedPostureCheck = PlannedPostureCheck.fromBundle(intent.extras!!)

    runBlocking {
      LatestNotificationTimestampRepository(context).setLastNotificationTimestamp(
        (System.currentTimeMillis() / 1000).toInt()
      )
    }

    val builder = NotificationCompat.Builder(context, kChecksNotificationChannel)
      .setSmallIcon(R.drawable.notification_icon)
      .setContentTitle(kNotificationText)
      .setPriority(NotificationCompat.PRIORITY_MAX)
      .addAction(R.drawable.ic_launcher_foreground, "Good", buildPendingIntentForReply(
        PostureCheckReply.GOOD, plannedPostureCheck, context))
      .addAction(R.drawable.ic_launcher_foreground, "Bad", buildPendingIntentForReply(
        PostureCheckReply.BAD, plannedPostureCheck, context))
      .addAction(R.drawable.ic_launcher_foreground, "Skip", buildPendingIntentForReply(
        PostureCheckReply.NOT_APPLICABLE, plannedPostureCheck, context))
    //builder.setOngoing(true)
    with(NotificationManagerCompat.from(context)) {
      if (ActivityCompat.checkSelfPermission(
          context,
          Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
      ) {
        // TODO: Consider calling
        // ActivityCompat#requestPermissions
        // here to request the missing permissions, and then overriding
        // public fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
        //                                        grantResults: IntArray)
        // to handle the case where the user grants the permission. See the documentation
        // for ActivityCompat#requestPermissions for more details.
        Log.i("tomek", "nie mam pozwolen")
        return@with
      }
      Log.i("tomek", "zawiadamiam")
      cancelAll()
      // notificationId is a unique int for each notification that you must define.
      notify(plannedPostureCheck.id, plannedPostureCheck.notificationId(), builder.build())
      context.sendBroadcast(Intent(context, RecomputeNextNotificationsBroadcastReceiver::class.java))
    }
  }
}