package com.tgarbus.posturecheck

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.tgarbus.posturecheck.data.PastPostureCheck
import com.tgarbus.posturecheck.data.PlannedPostureCheck
import com.tgarbus.posturecheck.data.PostureCheckReply

object NotificationConstants {
  // TODO: to niepoważne
  const val channelId = "kanał sport"
}

class NotificationAlarmBroadcastReceiver : BroadcastReceiver() {

  private fun createNotificationChannel(context: Context) {
    // TODO: set up channel better
    val name = "my notification channel"
    val descriptionText = "my freaking notification channel"
    val importance = NotificationManager.IMPORTANCE_DEFAULT
    val channel = NotificationChannel(NotificationConstants.channelId, name, importance).apply {
      description = descriptionText
    }
    // Register the channel with the system.
    val notificationManager: NotificationManager =
      context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
  }

  private fun buildPendingIntentForReply(
    reply: PostureCheckReply, plannedPostureCheck: PlannedPostureCheck,
    context: Context): PendingIntent {
    val baseIntent = Intent(context, NotificationResponseService::class.java)
    return baseIntent.let { intent ->
      intent.putExtras(plannedPostureCheck.withReply(reply).toBundle())
      // TODO: Rewrite construction of request code to make it clear there can be no collision.
      val requestCode = plannedPostureCheck.notificationId() + reply.ordinal
      PendingIntent.getService(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
    }
  }

  override fun onReceive(context: Context, intent: Intent) {
    createNotificationChannel(context)

    val plannedPostureCheck = PlannedPostureCheck.fromBundle(intent.extras!!)
    Log.i("tomek", "NotificationAlarmBroadcastReceiver: PlannedPostureCheck: " + plannedPostureCheck.toString())

    val builder = NotificationCompat.Builder(context, NotificationConstants.channelId)
      .setSmallIcon(R.drawable.ic_launcher_foreground)
      .setContentTitle("Hey, how's your posture?")
      .setPriority(NotificationCompat.PRIORITY_MAX)
      .addAction(R.drawable.ic_launcher_foreground, "Good", buildPendingIntentForReply(
        PostureCheckReply.GOOD, plannedPostureCheck, context))
      .addAction(R.drawable.ic_launcher_foreground, "Bad", buildPendingIntentForReply(
        PostureCheckReply.BAD, plannedPostureCheck, context))
      .addAction(R.drawable.ic_launcher_foreground, "N/A", buildPendingIntentForReply(
        PostureCheckReply.NOT_APPLICABLE, plannedPostureCheck, context))
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
      // notificationId is a unique int for each notification that you must define.
      notify(plannedPostureCheck.notificationId(), builder.build())
      context.sendBroadcast(Intent(context, RecomputeNextNotificationsBroadcastReceiver::class.java))
    }
  }
}