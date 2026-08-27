package com.kasirpro.ui.main

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.painter.Painter
import androidx.navigation.compose.*
import androidx.navigation.compose.rememberNavController
import com.kasirpro.ui.dashboard.DashboardScreen
import com.kasirpro.ui.transaction.TransactionScreen
import com.kasirpro.ui.report.ReportScreen
import com.kasirpro.ui.settings.SettingsScreen
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.assessment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.vector.ImageVector
import com.kasirpro.R

/**
 * MainActivity — hosts BottomNavigation with 4 tabs:
 * Dashboard, Transaksi, Laporan, Pengaturan
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem(stringResource(R.string.nav_dashboard), Icons.Default.Home) {
            navController.navigate("dashboard") { launchSingleTop = true; restoreState = true }
        },
        BottomNavItem(stringResource(R.string.nav_transaction), Icons.Default.ShoppingCart) {
            navController.navigate("transaction") { launchSingleTop = true; restoreState = true }
        },
        BottomNavItem(stringResource(R.string.nav_report), Icons.Default.assessment) {
            navController.navigate("report") { launchSingleTop = true; restoreState = true }
        },
        BottomNavItem(stringResource(R.string.nav_settings), Icons.Default.Settings) {
            navController.navigate("settings") { launchSingleTop = true; restoreState = true }
        },
    )
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = { item.onClick() },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(padding),
        ) {
            composable("dashboard") { DashboardScreen(onNavigateToProduct = {}, onNavigateToTable = {}) }
            composable("transaction") { TransactionScreen() }
            composable("report") { ReportScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String = label,
    val onClick: () -> Unit,
)
