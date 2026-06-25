package com.translatex.ai.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.translatex.ai.ui.screens.*
import com.translatex.ai.viewmodel.TranslationViewModel

sealed class Screen(val route: String) {
    object Splash   : Screen("splash")
    object Home     : Screen("home")
    object History  : Screen("history")
    object Favorites: Screen("favorites")
    object Settings : Screen("settings")
}

@Composable
fun TranslateXNavGraph(
    navController: NavHostController,
    viewModel: TranslationViewModel
) {
    NavHost(
        navController  = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(onFinished = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            })
        }

        composable(Screen.Home.route) {
            HomeScreen(
                viewModel   = viewModel,
                onNavigateToHistory   = { navController.navigate(Screen.History.route) },
                onNavigateToSettings  = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                viewModel  = viewModel,
                onBack     = { navController.popBackStack() },
                onReuse    = {
                    viewModel.loadFromHistory(it)
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                viewModel  = viewModel,
                onBack     = { navController.popBackStack() },
                onReuse    = {
                    viewModel.loadFromHistory(it)
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                onBack    = { navController.popBackStack() }
            )
        }
    }
}
