package com.cascadesim.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.cascadesim.ui.decisions.DecisionScreen
import com.cascadesim.ui.events.EventFeedScreen
import com.cascadesim.ui.home.HomeScreen

/**
 * Navigation graph for the application.
 * Defines all destinations and their routes.
 */
@Composable
fun NavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = NavRoute.Home.route
    ) {
        composable(NavRoute.Home.route) {
            HomeScreen(
                onNavigateToDecisions = {
                    navController.navigate(NavRoute.Decisions.route)
                },
                onNavigateToEvents = {
                    navController.navigate(NavRoute.Events.route)
                }
            )
        }
        
        composable(NavRoute.Decisions.route) {
            DecisionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(NavRoute.Events.route) {
            EventFeedScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
