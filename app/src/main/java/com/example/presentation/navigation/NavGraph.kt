package com.example.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.presentation.MainViewModel
import com.example.presentation.components.BottomNavigationBar
import com.example.presentation.screens.*
import com.example.ui.theme.DarkBackground

@Composable
fun AppNavGraph(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route

    val snackbarHostState = remember { SnackbarHostState() }
    val userMessage by viewModel.userMessage.collectAsState()

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    val showBottomBar = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.Stores.route,
        Screen.Apps.route,
        Screen.Settings.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            val navigateToTopLevel = { route: String ->
                navController.navigate(route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }

            // Screen 1: Dashboard
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToStores = { navigateToTopLevel(Screen.Stores.route) },
                    onNavigateToApps = { navigateToTopLevel(Screen.Apps.route) },
                    onNavigateToAlerts = { navController.navigate(Screen.Alerts.route) },
                    onNavigateToStatistics = { navController.navigate(Screen.Statistics.route) },
                    onNavigateToAppDetail = { pkg -> navController.navigate(Screen.AppDetail.createRoute(pkg)) }
                )
            }

            // Screen 2: Stores List
            composable(Screen.Stores.route) {
                StoresScreen(
                    viewModel = viewModel,
                    onNavigateToStoreDetail = { storeId -> navController.navigate(Screen.StoreDetail.createRoute(storeId)) },
                    onNavigateToAddStore = { navController.navigate(Screen.AddStore.route) }
                )
            }

            // Screen 3: Store Detail
            composable(
                route = Screen.StoreDetail.route,
                arguments = listOf(navArgument("storeId") { type = NavType.StringType })
            ) { backStackEntry ->
                val storeId = backStackEntry.arguments?.getString("storeId") ?: ""
                StoreDetailScreen(
                    storeId = storeId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToStoreSettings = { id -> navController.navigate(Screen.StoreSettings.createRoute(id)) },
                    onNavigateToAppDetail = { pkg -> navController.navigate(Screen.AppDetail.createRoute(pkg)) }
                )
            }

            // Screen 4: Apps List
            composable(Screen.Apps.route) {
                AppsScreen(
                    viewModel = viewModel,
                    onNavigateToAppDetail = { pkg -> navController.navigate(Screen.AppDetail.createRoute(pkg)) }
                )
            }

            // Screen 5: App Detail
            composable(
                route = Screen.AppDetail.route,
                arguments = listOf(navArgument("packageName") { type = NavType.StringType })
            ) { backStackEntry ->
                val packageName = backStackEntry.arguments?.getString("packageName") ?: ""
                AppDetailScreen(
                    packageName = packageName,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToReleaseDetail = { pkg, track, vCode ->
                        navController.navigate(Screen.ReleaseDetail.createRoute(pkg, track, vCode))
                    }
                )
            }

            // Screen 6: Release Detail
            composable(
                route = Screen.ReleaseDetail.route,
                arguments = listOf(
                    navArgument("packageName") { type = NavType.StringType },
                    navArgument("trackName") { type = NavType.StringType },
                    navArgument("versionCode") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val packageName = backStackEntry.arguments?.getString("packageName") ?: ""
                val trackName = backStackEntry.arguments?.getString("trackName") ?: ""
                val versionCode = backStackEntry.arguments?.getInt("versionCode") ?: 100
                ReleaseDetailScreen(
                    packageName = packageName,
                    trackName = trackName,
                    versionCode = versionCode,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // Screen 7: Add Store
            composable(Screen.AddStore.route) {
                AddStoreScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onSuccess = { navController.popBackStack() }
                )
            }

            // Screen 8: Store Settings
            composable(
                route = Screen.StoreSettings.route,
                arguments = listOf(navArgument("storeId") { type = NavType.StringType })
            ) { backStackEntry ->
                val storeId = backStackEntry.arguments?.getString("storeId") ?: ""
                StoreSettingsScreen(
                    storeId = storeId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // Screen 9: Release History
            composable(Screen.ReleaseHistory.route) {
                ReleaseHistoryScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToReleaseDetail = { pkg, track, vCode ->
                        navController.navigate(Screen.ReleaseDetail.createRoute(pkg, track, vCode))
                    }
                )
            }

            // Screen 10: Alerts
            composable(Screen.Alerts.route) {
                AlertsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // Screen 11: Statistics
            composable(Screen.Statistics.route) {
                StatisticsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // Screen 12: Settings
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateToStores = { navigateToTopLevel(Screen.Stores.route) },
                    onNavigateToAddStore = { navController.navigate(Screen.AddStore.route) }
                )
            }
        }
    }
}
