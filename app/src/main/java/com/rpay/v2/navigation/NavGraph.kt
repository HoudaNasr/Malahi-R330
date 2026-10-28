package com.rpay.v2.navigation

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rpay.v2.features.adminscreen.presentation.AdminSettingsScreen
import com.rpay.v2.features.kiosk.presentation.KioskScreen
import com.rpay.v2.features.settings.presentation.DeviceSettingsRoute


@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        // ADMIN SETTINGS screen
        composable(Destination.AdminSettingsScreen.route) {
            AdminSettingsScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(    route = "settings?timeout={timeout}",
            arguments = listOf(
                navArgument("timeout") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->

            val timeout = backStackEntry
                .arguments
                ?.getString("timeout")
                ?.toIntOrNull()

            DeviceSettingsRoute(
                timeout = timeout,
                onBack = {
                    navController.popBackStack()
                },
                onOpenAdminSettings = {
                    navController.navigate(Destination.AdminSettingsScreen.route)
                    Log.e("DeviceSettingsRoute", "onOpenAdminSettings")
                },
                onTestMdb = {
                    Log.e("DeviceSettingsRoute", "onTestMdb")
                },
                onTestPulse = {
                    Log.e("DeviceSettingsRoute", "onTestPulse")
                },
                onReboot = {
                    Log.e("DeviceSettingsRoute", "onReboot")
                },
            )
        }

        composable(Destination.KioskMachine.route) {
            KioskScreen (
                onOpenSettings = {
                    navController.navigate("settings")
                }
            )
        }
    }
}

// Placeholder screen for unimplemented features
@Composable
fun PlaceholderScreen(title: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack) {
            Text("Back")
        }
    }
}