package com.kasirpro.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.*
import androidx.navigation.compose.rememberNavController
import com.kasirpro.ui.dashboard.DashboardScreen
import com.kasirpro.ui.transaction.TransactionScreen
import com.kasirpro.ui.report.ReportScreen
import com.kasirpro.ui.settings.SettingsScreen
import com.kasirpro.ui.product.ProductScreen
import com.kasirpro.ui.product.AddProductScreen
import com.kasirpro.ui.table.TableScreen
import com.kasirpro.ui.table.AddTableScreen
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Settings
import com.kasirpro.data.model.Product
import com.kasirpro.R

private val bottomNavRoutes = setOf("dashboard", "transaction", "report", "settings")

/**
 * MainScreen — hosts BottomNavigation with 4 tabs:
 * Dashboard, Transaksi, Laporan, Pengaturan
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem(stringResource(R.string.nav_dashboard), Icons.Default.Home, "dashboard") {
            navController.navigate("dashboard") { launchSingleTop = true; restoreState = true }
        },
        BottomNavItem(stringResource(R.string.nav_transaction), Icons.Default.ShoppingCart, "transaction") {
            navController.navigate("transaction") { launchSingleTop = true; restoreState = true }
        },
        BottomNavItem(stringResource(R.string.nav_report), Icons.Default.Analytics, "report") {
            navController.navigate("report") { launchSingleTop = true; restoreState = true }
        },
        BottomNavItem(stringResource(R.string.nav_settings), Icons.Default.Settings, "settings") {
            navController.navigate("settings") { launchSingleTop = true; restoreState = true }
        },
    )
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
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
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(padding),
        ) {
            composable("dashboard") {
                DashboardScreen(
                    onNavigateToProduct = { navController.navigate("product") },
                    onNavigateToTable = { navController.navigate("table") },
                    onAddToCart = { productId ->
                        navController.navigate("transaction")
                    },
                )
            }
            composable("transaction") { TransactionScreen() }
            composable("report") { ReportScreen() }
            composable("settings") { SettingsScreen() }
            composable("product") {
                ProductScreen(
                    onEditProduct = { product: Product ->
                        navController.navigate("add_product/${product.id}")
                    },
                    onAddProduct = { navController.navigate("add_product") },
                )
            }
            composable("add_product") {
                AddProductScreen(onProductSaved = { navController.popBackStack() })
            }
            composable("add_product/{productId}") { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId")?.toLongOrNull()
                AddProductScreen(
                    editProductId = productId,
                    onProductSaved = { navController.popBackStack() },
                )
            }
            composable("table") {
                TableScreen(
                    onAddTable = { navController.navigate("add_table") },
                    onEditTable = { table: com.kasirpro.data.model.Table ->
                        navController.navigate("add_table/${table.id}")
                    },
                )
            }
            composable("add_table") {
                AddTableScreen(onTableSaved = { navController.popBackStack() })
            }
            composable("add_table/{tableId}") { backStackEntry ->
                val tableId = backStackEntry.arguments?.getString("tableId")?.toLongOrNull()
                AddTableScreen(
                    editTableId = tableId,
                    onTableSaved = { navController.popBackStack() },
                )
            }
        }
    }
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String,
    val onClick: () -> Unit,
)
