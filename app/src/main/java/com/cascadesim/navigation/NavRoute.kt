package com.cascadesim.navigation

/**
 * Navigation routes for the application.
 */
sealed class NavRoute(val route: String) {
    object Home : NavRoute("home")
    object Decisions : NavRoute("decisions")
    object Events : NavRoute("events")
}
