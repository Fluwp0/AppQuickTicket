package com.quickticket.app.navigation

sealed class NavRoute(val route: String) {

    data object Login : NavRoute("login")
    data object Register : NavRoute("register")
    data object Home : NavRoute("home")
    data object Profile : NavRoute("profile")
    data object Cart : NavRoute("cart")
    object Products : NavRoute("products")

}
