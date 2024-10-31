package com.tgarbus.posturecheck

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tgarbus.posturecheck.ui.theme.PostureCheckTheme
import com.tgarbus.posturecheck.ui.views.AdminPage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // How about doing HorizontalPager instead for different screens?

        setContent {
            val navController = rememberNavController()
            PostureCheckTheme {
                NavHost(
                    navController = navController,
                    startDestination = "admin",
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None },
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable("admin") {
                        AdminPage()
                    }
                }
            }
        }
        val intent = Intent(baseContext, RecomputeNextNotificationsBroadcastReceiver::class.java)
        sendBroadcast(intent)
    }
}
