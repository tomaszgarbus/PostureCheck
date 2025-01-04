package com.tgarbus.posturecheck

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

const val kTestNotificationId = -1
const val kNotificationText = "Hey, how's your posture?"
const val kChecksNotificationChannel = "kanał sport"

fun createNotificationChannel(context: Context) {
    // TODO: set up channel better
    val name = "my notification channel"
    val descriptionText = "my freaking notification channel"
    val importance = NotificationManager.IMPORTANCE_DEFAULT
    val channel = NotificationChannel(kChecksNotificationChannel, name, importance).apply {
        description = descriptionText
    }
    // Register the channel with the system.
    val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
}

// Builds a pending intent to be executed when user takes any action. The notification will then
// be dismissed by NotificationResponseService.
fun buildPendingIntentForDismissal(context: Context): PendingIntent {
    val baseIntent = Intent(context, NotificationResponseService::class.java)
    return baseIntent.let { intent ->
        val bundle = Bundle()
        bundle.putBoolean("is_test_notification", true)
        intent.putExtras(bundle)
        // TODO: Comment why FLAG_UPDATE_CURRENT and FLAG_MUTABLE are needed.
        val requestCode = -1
        PendingIntent.getService(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
    }
}

fun sendTestNotification(context: Context) {
    val builder = NotificationCompat.Builder(context, kChecksNotificationChannel)
        .setSmallIcon(R.drawable.notification_icon)
        .setContentTitle(kNotificationText)
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .addAction(R.drawable.ic_launcher_foreground, "Good",
            buildPendingIntentForDismissal(context))
        .addAction(R.drawable.ic_launcher_foreground, "Bad",
            buildPendingIntentForDismissal(context))
        .addAction(R.drawable.ic_launcher_foreground, "N/A",
            buildPendingIntentForDismissal(context))

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
        notify(kTestNotificationId, builder.build())
        context.sendBroadcast(Intent(context, RecomputeNextNotificationsBroadcastReceiver::class.java))
    }
}
