package com.tgarbus.posturecheck

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationService : Service() {

  // TODO: to niepoważne
  private val channelId = "kanał sport"

  override fun onCreate() {
    super.onCreate()
    Log.i("tomek", "onCreate")
  }

  private fun createNotificationChannel() {
    // TODO: set up channel better
    val name = "my notification channel"
    val descriptionText = "my freaking notification channel"
    val importance = NotificationManager.IMPORTANCE_DEFAULT
    val channelId = "kanał sport"
    val channel = NotificationChannel(channelId, name, importance).apply {
      description = descriptionText
    }
    // Register the channel with the system.
    val notificationManager: NotificationManager =
      getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    val res = super.onStartCommand(intent, flags, startId)
    Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show()
    Log.i("tomek", "onStartCommand")

    createNotificationChannel()
    Log.i("tomek", "stworzylem kanał")

    val builder = NotificationCompat.Builder(this, "kanał sport")
      .setSmallIcon(R.drawable.ic_launcher_foreground)
      .setContentTitle("siema")
      .setContentText("elo gitara")
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    val NOTIFICATION_ID = 10
    with(NotificationManagerCompat.from(this)) {
      if (ActivityCompat.checkSelfPermission(
          this@NotificationService,
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
      notify(NOTIFICATION_ID, builder.build())
    }

    return res
  }

  override fun onBind(p0: Intent?): IBinder? {
    return null
  }
}