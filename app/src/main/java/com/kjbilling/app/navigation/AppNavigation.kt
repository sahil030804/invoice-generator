package com.kjbilling.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kjbilling.app.KJInvoiceApp
import com.kjbilling.app.ui.customer.CustomerFormScreen
import com.kjbilling.app.ui.customer.CustomerListScreen
import com.kjbilling.app.ui.dashboard.DashboardScreen
import com.kjbilling.app.ui.invoice.create.InvoiceCreateScreen
import com.kjbilling.app.ui.invoice.create.InvoiceShareScreen
import com.kjbilling.app.ui.invoice.detail.InvoiceDetailScreen
import com.kjbilling.app.ui.invoice.history.InvoiceHistoryScreen
import com.kjbilling.app.ui.onboarding.OnboardingScreen
import com.kjbilling.app.ui.product.ProductFormScreen
import com.kjbilling.app.ui.product.ProductListScreen
import com.kjbilling.app.ui.settings.BusinessProfileScreen
import com.kjbilling.app.ui.settings.SettingsScreen
import kotlinx.coroutines.flow.map

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Dashboard : Screen("dashboard")
    object CustomerList : Screen("customers")
    object CustomerForm : Screen("customer_form?id={id}") {
        fun createRoute(id: Long?) = if (id != null) "customer_form?id=$id" else "customer_form"
    }
    object ProductList : Screen("products")
    object ProductForm : Screen("product_form?id={id}") {
        fun createRoute(id: Long?) = if (id != null) "product_form?id=$id" else "product_form"
    }
    object InvoiceCreate : Screen("invoice_create?id={id}") {
        fun createRoute(id: Long? = null) = if (id != null) "invoice_create?id=$id" else "invoice_create"
    }
    object QuickBill : Screen("quick_bill")
    object InvoiceHistory : Screen("invoice_history")
    object InvoiceDetail : Screen("invoice_detail/{id}") {
        fun createRoute(id: Long) = "invoice_detail/$id"
    }
    object InvoiceShare : Screen("invoice_share/{invoiceId}/{filePath}") {
        fun createRoute(invoiceId: Long, filePath: String) = "invoice_share/$invoiceId/${java.net.URLEncoder.encode(filePath, "UTF-8")}"
    }
    object Settings : Screen("settings")
    object BusinessProfile : Screen("business_profile")
}

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard.route, "Dashboard", Icons.Filled.Home, "Dashboard"),
    BottomNavItem(Screen.CustomerList.route, "Customers", Icons.Filled.Person, "Customers"),
    BottomNavItem(Screen.ProductList.route, "Products", Icons.Filled.ShoppingCart, "Products"),
    BottomNavItem(Screen.InvoiceHistory.route, "History", Icons.Filled.List, "History"),
    BottomNavItem(Screen.Settings.route, "Settings", Icons.Filled.Settings, "Settings")
)

private val topLevelRoutes = setOf(
    Screen.Dashboard.route,
    Screen.CustomerList.route,
    Screen.ProductList.route,
    Screen.InvoiceHistory.route,
    Screen.Settings.route
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val app = context.applicationContext as KJInvoiceApp
    val settingsRepo = app.container.appSettingsRepository

    val settings by settingsRepo.getSettings().map { it as com.kjbilling.app.domain.model.AppSettings? }
        .collectAsState(initial = null)

    if (settings == null) {
        // Still loading settings from DB; render nothing to avoid nav flicker.
        return
    }

    val startDestination = if (settings?.onboardingCompleted == true) {
        Screen.Dashboard.route
    } else {
        Screen.Onboarding.route
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isTopLevel = currentRoute in topLevelRoutes

    Scaffold(
        bottomBar = {
            if (isTopLevel) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.contentDescription) },
                            label = { Text(item.label) },
                            selected = selected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding()),
            enterTransition = {
                val targetRoute = targetState.destination.route
                val initialRoute = initialState.destination.route
                if (targetRoute in topLevelRoutes && initialRoute in topLevelRoutes) {
                    fadeIn(animationSpec = tween(220))
                } else {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                }
            },
            exitTransition = {
                val targetRoute = targetState.destination.route
                val initialRoute = initialState.destination.route
                if (targetRoute in topLevelRoutes && initialRoute in topLevelRoutes) {
                    fadeOut(animationSpec = tween(180))
                } else {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                }
            },
            popEnterTransition = {
                val targetRoute = targetState.destination.route
                val initialRoute = initialState.destination.route
                if (targetRoute in topLevelRoutes && initialRoute in topLevelRoutes) {
                    fadeIn(animationSpec = tween(220))
                } else {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                }
            },
            popExitTransition = {
                val targetRoute = targetState.destination.route
                val initialRoute = initialState.destination.route
                if (targetRoute in topLevelRoutes && initialRoute in topLevelRoutes) {
                    fadeOut(animationSpec = tween(180))
                } else {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                }
            }
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onOnboardingComplete = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onNavigateToNewInvoice = { navController.navigate(Screen.InvoiceCreate.route) },
                    onNavigateToQuickBill = { navController.navigate(Screen.QuickBill.route) },
                    onNavigateToInvoiceDetail = { id -> navController.navigate(Screen.InvoiceDetail.createRoute(id)) },
                    onNavigate = { route ->
                        when (route) {
                            "customers" -> navController.navigate(Screen.CustomerList.route)
                            "products" -> navController.navigate(Screen.ProductList.route)
                            "history", "invoice_history" -> navController.navigate(Screen.InvoiceHistory.route)
                            "settings" -> navController.navigate(Screen.Settings.route)
                        }
                    }
                )
            }

            composable(Screen.QuickBill.route) {
                com.kjbilling.app.ui.invoice.quick.QuickBillScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDetail = { id -> navController.navigate(Screen.InvoiceDetail.createRoute(id)) }
                )
            }

            composable(Screen.CustomerList.route) {
                CustomerListScreen(
                    onNavigateToEdit = { id -> navController.navigate(Screen.CustomerForm.createRoute(id)) },
                    onNavigateToAdd = { navController.navigate(Screen.CustomerForm.createRoute(null)) }
                )
            }

            composable(
                route = Screen.CustomerForm.route,
                arguments = listOf(navArgument("id") { type = NavType.StringType; nullable = true })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")?.toLongOrNull()
                CustomerFormScreen(
                    customerId = id,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.ProductList.route) {
                ProductListScreen(
                    onNavigateToEdit = { id -> navController.navigate(Screen.ProductForm.createRoute(id)) },
                    onNavigateToAdd = { navController.navigate(Screen.ProductForm.createRoute(null)) }
                )
            }

            composable(
                route = Screen.ProductForm.route,
                arguments = listOf(navArgument("id") { type = NavType.StringType; nullable = true })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")?.toLongOrNull()
                ProductFormScreen(
                    productId = id,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.InvoiceCreate.route,
                arguments = listOf(navArgument("id") { type = NavType.StringType; nullable = true })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")?.toLongOrNull()
                InvoiceCreateScreen(
                    invoiceId = id,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToShare = { invoiceId, path ->
                        navController.navigate(Screen.InvoiceShare.createRoute(invoiceId, path)) {
                            popUpTo(Screen.InvoiceCreate.route) { inclusive = true }
                        }
                    },
                    onNavigateToNewCustomer = { navController.navigate(Screen.CustomerForm.createRoute(null)) }
                )
            }

            composable(Screen.InvoiceHistory.route) {
                InvoiceHistoryScreen(
                    onNavigateToDetail = { id -> navController.navigate(Screen.InvoiceDetail.createRoute(id)) }
                )
            }

            composable(
                route = Screen.InvoiceDetail.route,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("id") ?: return@composable
                InvoiceDetailScreen(
                    invoiceId = id,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { editId -> navController.navigate(Screen.InvoiceCreate.createRoute(editId)) }
                )
            }

            composable(
                route = Screen.InvoiceShare.route,
                arguments = listOf(
                    navArgument("invoiceId") { type = NavType.LongType },
                    navArgument("filePath") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("invoiceId") ?: return@composable
                val path = backStackEntry.arguments?.getString("filePath") ?: return@composable
                val decodedPath = java.net.URLDecoder.decode(path, "UTF-8")
                InvoiceShareScreen(
                    invoiceId = id,
                    filePath = decodedPath,
                    onNavigateHome = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(0)
                        }
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToBusinessProfile = { navController.navigate(Screen.BusinessProfile.route) }
                )
            }

            composable(Screen.BusinessProfile.route) {
                BusinessProfileScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
