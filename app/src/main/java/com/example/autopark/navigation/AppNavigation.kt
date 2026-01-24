package com.example.autopark.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.autopark.ui.screens.auth.LoginScreen
import com.example.autopark.ui.screens.auth.RegisterScreen
import com.example.autopark.ui.screens.dashboard.AdminDashboardScreen
import com.example.autopark.ui.screens.dashboard.DriverDashboardScreen
import com.example.autopark.ui.viewmodel.AuthViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val currentUser = authViewModel.currentUser.value
        startDestination = if (currentUser != null) {
            when (currentUser.role) {
                "admin" -> "admin_dashboard"
                else -> "driver_dashboard"
            }
        } else {
            "login"
        }
    }

    if (startDestination == null) {
        // Show a loading screen while determining the start destination
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        NavHost(
            navController = navController,
            startDestination = startDestination!!
        ) {
            composable("login") {
                LoginScreen(navController = navController)
            }
            composable("register") {
                RegisterScreen(navController = navController)
            }
            composable("admin_dashboard") {
                AdminDashboardScreen(navController = navController)
            }
            composable("driver_dashboard") {
                DriverDashboardScreen(navController = navController)
            }
        }
    }
}
