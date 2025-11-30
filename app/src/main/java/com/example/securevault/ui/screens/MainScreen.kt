package com.example.securevault.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.securevault.ui.viewmodel.MainViewModel

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onAddClick = { navController.navigate("add_edit/-1") },
                onItemClick = { id -> navController.navigate("add_edit/$id") },
                onSettingsClick = { navController.navigate("settings") }
            )
        }
        composable(
            "add_edit/{credentialId}",
            arguments = listOf(navArgument("credentialId") { type = NavType.IntType })
        ) { backStackEntry ->
            val credentialId = backStackEntry.arguments?.getInt("credentialId") ?: -1
            AddEditScreen(
                viewModel = viewModel,
                credentialId = credentialId,
                onBack = { navController.popBackStack() }
            )
        }
        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
