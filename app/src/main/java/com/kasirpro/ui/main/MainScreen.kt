package com.kasirpro.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val labelDashboard = stringResource(R.string.nav_dashboard)
    val labelTransaction = stringResource(R.string.nav_transaction)
    val labelReport = stringResource(R.string.nav_report)
    val labelSettings = stringResource(R.string.nav_settings)

    // FIX pelan/lebay: items jangan dibikin tiap recompose — remember sekali
    val items = remember(labelDashboard, labelTransaction, labelReport, labelSettings) {
        listOf(
            BottomNavItem(labelDashboard, Icons.Default.Home, "dashboard"),
            BottomNavItem(labelTransaction, Icons.Default.ShoppingCart, "transaction"),
            BottomNavItem(labelReport, Icons.Default.Analytics, "report"),
            BottomNavItem(labelSettings, Icons.Default.Settings, "settings"),
        )
    }

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
                            onClick = {
                                navController.navigate(item.route) {
                                    // FIX lebay: popUpTo + saveState biar gak numpuk backstack & gak reload
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
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
                    onAddToCart = { _ -> navController.navigate("transaction") {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true; restoreState = true
                    }},
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
)
