package com.tgarbus.posturecheck

import android.Manifest
import android.content.Intent
import android.content.Intent.ACTION_USER_FOREGROUND
import android.content.Intent.ACTION_USER_PRESENT
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tgarbus.posturecheck.data.OnboardingRepository
import com.tgarbus.posturecheck.ui.views.AboutPage
import com.tgarbus.posturecheck.ui.views.AdminPage
import com.tgarbus.posturecheck.ui.views.InAppPostureCheckContainer
import com.tgarbus.posturecheck.ui.views.NavigationFloat
import com.tgarbus.posturecheck.ui.views.NavigationPage
import com.tgarbus.posturecheck.ui.views.NotificationsPage
import com.tgarbus.posturecheck.ui.views.OnboardingPage
import com.tgarbus.posturecheck.ui.views.SettingsPage
import com.tgarbus.posturecheck.ui.views.StatisticsPage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel(baseContext)

        enableEdgeToEdge()

        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = true

        var isIntroScreenCompleted: Boolean
        runBlocking {
            isIntroScreenCompleted = OnboardingRepository(
                baseContext).isIntroScreenCompleted().first()
        }

        val filter = IntentFilter()
        filter.addAction(ACTION_USER_PRESENT)
        filter.addAction(ACTION_USER_FOREGROUND)
        ContextCompat.registerReceiver(baseContext, RecomputeNextNotificationsBroadcastReceiver(),
            filter, ContextCompat.RECEIVER_EXPORTED)

        setContent {
            val navController = rememberNavController()
            val startingPoint = if (isIntroScreenCompleted) "main/STATISTICS" else "onboarding"
            InAppPostureCheckContainer {
                NavHost(
                    navController = navController,
                    startDestination = startingPoint,
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None },
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable("main/{currentPage}", arguments = listOf(
                        navArgument(name = "currentPage") {
                            type = NavType.StringType
                            defaultValue = NavigationPage.STATISTICS.toString()
                        }
                    )) { backStackEntry ->
                        val currentPage = NavigationPage.valueOf(backStackEntry.arguments!!.getString("currentPage")!!)
                        when (currentPage) {
                            NavigationPage.ADMIN -> AdminPage(navController)
                            NavigationPage.STATISTICS -> StatisticsPage(navController)
                            NavigationPage.ABOUT -> AboutPage()
                            NavigationPage.SETTINGS -> SettingsPage(
                                triggerRecompute = {
                                    val intent = Intent(
                                        baseContext,
                                        RecomputeNextNotificationsBroadcastReceiver::class.java
                                    )
                                    sendBroadcast(intent)
                                },
                                openNotificationSettings = {
                                    val settingsIntent: Intent =
                                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                                    ContextCompat.startActivity(baseContext, settingsIntent, null)
                                },
                                navController = navController
                            )
                        }
                        NavigationFloat(
                            currentPage = currentPage,
                            onPageChanged = {
                                navController.navigate("main/$it")
                            })
                    }
                    composable("notifications") {
                        NotificationsPage(navController)
                    }
                    composable("onboarding") {
                        OnboardingPage(
                            navController,
                            showNotificationAboutExactAlarm = (
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                                ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.SCHEDULE_EXACT_ALARM) == PackageManager.PERMISSION_DENIED
                            ),
                            requestExactAlarmPermissions = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    startActivity(Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                                }
                            },
                            triggerRecompute = {
                                val intent = Intent(
                                    baseContext,
                                    RecomputeNextNotificationsBroadcastReceiver::class.java
                                )
                                sendBroadcast(intent)
                            })
                    }
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)) {
                PackageManager.PERMISSION_GRANTED -> {}
                PackageManager.PERMISSION_DENIED -> {
                    requestPermissions(listOf(Manifest.permission.POST_NOTIFICATIONS).toTypedArray(), 1)
                }
            }
        }
        /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            when (ContextCompat.checkSelfPermission(this, Manifest.permission.SCHEDULE_EXACT_ALARM)) {
                PackageManager.PERMISSION_GRANTED -> {}
                PackageManager.PERMISSION_DENIED -> {
                    startActivity(Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                }
            }
        } */
        val intent = Intent(baseContext, RecomputeNextNotificationsBroadcastReceiver::class.java)
        sendBroadcast(intent)
    }
}
