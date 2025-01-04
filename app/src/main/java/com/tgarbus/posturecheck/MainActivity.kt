package com.tgarbus.posturecheck

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tgarbus.posturecheck.data.OnboardingRepository
import com.tgarbus.posturecheck.ui.views.AboutPage
import com.tgarbus.posturecheck.ui.views.AdminPage
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

        setContent {
            val navController = rememberNavController()
            val startingPoint = if (isIntroScreenCompleted) "main" else "onboarding"
            NavHost(navController = navController,
                startDestination = startingPoint,
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                modifier = Modifier.fillMaxSize()) {
                composable("main") {
                    val currentPage = remember { mutableStateOf(NavigationPage.STATISTICS) }
                    when (currentPage.value) {
                        NavigationPage.ADMIN -> AdminPage(navController)
                        NavigationPage.STATISTICS -> StatisticsPage(navController)
                        NavigationPage.ABOUT -> AboutPage()
                        NavigationPage.SETTINGS -> SettingsPage(triggerRecompute = {
                                val intent = Intent(
                                    baseContext, RecomputeNextNotificationsBroadcastReceiver::class.java)
                                sendBroadcast(intent)
                            },
                            openNotificationSettings = {
                                val settingsIntent: Intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                                ContextCompat.startActivity(baseContext, settingsIntent, null)
                            },
                            navController = navController)
                    }
                    NavigationFloat(
                        currentPage = currentPage.value,
                        onPageChanged = {
                            currentPage.value = it
                        })
                }
                composable("notifications") {
                    NotificationsPage(navController)
                }
                composable("onboarding") {
                    OnboardingPage(navController)
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
        val intent = Intent(baseContext, RecomputeNextNotificationsBroadcastReceiver::class.java)
        sendBroadcast(intent)
    }
}
