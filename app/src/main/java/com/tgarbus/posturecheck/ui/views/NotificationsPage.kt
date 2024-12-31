package com.tgarbus.posturecheck.ui.views

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.tgarbus.posturecheck.ui.reusables.PageHeader
import com.tgarbus.posturecheck.ui.reusables.ScrollableFullScreenColumn

@Composable
fun NotificationsPage(navController: NavController) {
    ScrollableFullScreenColumn {
        PageHeader("Notifications")
        Button(onClick = { navController.popBackStack() }) { Text("<") }
    }
}