package com.example.basketballhelp.ui.navigation

sealed class AppRoute(val route: String) {
    data object Dashboard : AppRoute("dashboard")
    data object Log : AppRoute("log")
    data object History : AppRoute("history")
    data object Drills : AppRoute("drills")
    data object Profile : AppRoute("profile")
    data object EditSession : AppRoute("edit/{sessionId}") {
        fun create(sessionId: Int) = "edit/$sessionId"
    }
}
