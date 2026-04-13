package fr.myefrei.nanoorbit.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import fr.myefrei.nanoorbit.ui.dashboard.DashboardScreen
import fr.myefrei.nanoorbit.ui.detail.DetailScreen
import fr.myefrei.nanoorbit.ui.map.MapScreen
import fr.myefrei.nanoorbit.ui.navigation.Routes
import fr.myefrei.nanoorbit.ui.planning.PlanningScreen
import fr.myefrei.nanoorbit.viewmodel.NanoOrbitViewModel

@Composable
fun NanoOrbitApp(
    viewModel: NanoOrbitViewModel = viewModel()
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute?.startsWith("detail/") != true

    Surface {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        bottomNavItems.forEach { item ->
                            NavigationBarItem(
                                selected = backStackEntry?.destination?.hierarchy
                                    ?.any { destination -> destination.route == item.route } == true,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label
                                    )
                                },
                                label = {
                                    Text(text = item.label)
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Routes.DASHBOARD
            ) {
                composable(route = Routes.DASHBOARD) {
                    DashboardScreen(
                        contentPadding = innerPadding,
                        viewModel = viewModel,
                        onSatelliteClick = { satelliteId ->
                            navController.navigate(Routes.detail(satelliteId))
                        }
                    )
                }

                composable(route = Routes.PLANNING) {
                    PlanningScreen(
                        contentPadding = innerPadding,
                        viewModel = viewModel
                    )
                }

                composable(route = Routes.MAP) {
                    MapScreen(
                        contentPadding = innerPadding,
                        viewModel = viewModel
                    )
                }

                composable(
                    route = Routes.DETAIL_PATTERN,
                    arguments = listOf(
                        navArgument("satelliteId") {
                            type = NavType.StringType
                        }
                    )
                ) { destination ->
                    DetailScreen(
                        satelliteId = destination.arguments?.getString("satelliteId").orEmpty(),
                        viewModel = viewModel,
                        onBackClick = navController::popBackStack
                    )
                }
            }
        }
    }
}

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(
        route = Routes.DASHBOARD,
        label = "Dashboard",
        icon = Icons.Default.SatelliteAlt
    ),
    BottomNavItem(
        route = Routes.PLANNING,
        label = "Planning",
        icon = Icons.Default.Public
    ),
    BottomNavItem(
        route = Routes.MAP,
        label = "Carte",
        icon = Icons.Default.Map
    )
)
