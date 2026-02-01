package com.zaheer.imagetopdf

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zaheer.imagetopdf.screens.HomeScreen
import com.zaheer.imagetopdf.screens.PreviewScreen
import com.zaheer.imagetopdf.screens.ProScreen
import com.zaheer.imagetopdf.screens.ResultScreen
import com.zaheer.imagetopdf.viewmodel.AppViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Preview : Screen("preview")
    object Result : Screen("result")
    object Pro : Screen("pro")
}

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val viewModel: AppViewModel = viewModel()

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToPreview = { navController.navigate(Screen.Preview.route) },
                onNavigateToPro = { navController.navigate(Screen.Pro.route) }
            )
        }
        composable(Screen.Preview.route) {
            PreviewScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToResult = { navController.navigate(Screen.Result.route) }
            )
        }
        composable(Screen.Result.route) {
            ResultScreen(
                viewModel = viewModel,
                onNavigateHome = {
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                }
            )
        }
        composable(Screen.Pro.route) {
            ProScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
