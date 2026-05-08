package com.example.basketballhelp.ui.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PostAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.basketballhelp.HoopDevApplication
import com.example.basketballhelp.ui.navigation.AppRoute
import com.example.basketballhelp.ui.screen.dashboard.DashboardScreen
import com.example.basketballhelp.ui.screen.dashboard.DashboardViewModel
import com.example.basketballhelp.ui.screen.drills.DrillsScreen
import com.example.basketballhelp.ui.screen.drills.DrillsViewModel
import com.example.basketballhelp.ui.screen.history.EditSessionScreen
import com.example.basketballhelp.ui.screen.history.EditSessionViewModel
import com.example.basketballhelp.ui.screen.history.HistoryScreen
import com.example.basketballhelp.ui.screen.history.HistoryViewModel
import com.example.basketballhelp.ui.screen.log.LogSessionScreen
import com.example.basketballhelp.ui.screen.log.LogSessionViewModel
import com.example.basketballhelp.ui.screen.profile.ProfileScreen
import com.example.basketballhelp.ui.screen.profile.ProfileViewModel

@Composable
fun HoopDevApp(app: HoopDevApplication) {
    val navController = rememberNavController()
    val factory = AppViewModelFactory(app)
    val bottomRoutes = listOf(
        Triple(AppRoute.Dashboard, "Dashboard", Icons.Rounded.Dashboard),
        Triple(AppRoute.Log, "Log", Icons.Rounded.PostAdd),
        Triple(AppRoute.History, "History", Icons.Rounded.History),
        Triple(AppRoute.Drills, "Drills", Icons.Rounded.FitnessCenter),
        Triple(AppRoute.Profile, "Profile", Icons.Rounded.Person),
    )
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        bottomBar = {
            if (!currentRoute.orEmpty().startsWith("edit/")) {
                NavigationBar {
                    bottomRoutes.forEach { (route, label, icon) ->
                        NavigationBarItem(
                            selected = currentRoute == route.route,
                            onClick = {
                                navController.navigate(route.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.Dashboard.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(AppRoute.Dashboard.route) {
                val viewModel: DashboardViewModel = viewModel(factory = factory)
                DashboardScreen(viewModel)
            }
            composable(AppRoute.Log.route) {
                val viewModel: LogSessionViewModel = viewModel(factory = factory)
                LogSessionScreen(viewModel, onSaved = {
                    navController.navigate(AppRoute.Dashboard.route) {
                        popUpTo(AppRoute.Dashboard.route) { inclusive = true }
                    }
                })
            }
            composable(AppRoute.History.route) {
                val viewModel: HistoryViewModel = viewModel(factory = factory)
                HistoryScreen(
                    viewModel = viewModel,
                    onEditSession = { navController.navigate(AppRoute.EditSession.create(it)) },
                )
            }
            composable(AppRoute.Drills.route) {
                val viewModel: DrillsViewModel = viewModel(factory = factory)
                DrillsScreen(viewModel)
            }
            composable(AppRoute.Profile.route) {
                val viewModel: ProfileViewModel = viewModel(factory = factory)
                ProfileScreen(viewModel)
            }
            composable(AppRoute.EditSession.route) { backStackEntry ->
                val viewModel: EditSessionViewModel = viewModel(factory = factory)
                val sessionId = backStackEntry.arguments?.getString("sessionId")?.toIntOrNull() ?: 0
                EditSessionScreen(
                    viewModel = viewModel,
                    sessionId = sessionId,
                    onSaved = { navController.popBackStack() },
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
