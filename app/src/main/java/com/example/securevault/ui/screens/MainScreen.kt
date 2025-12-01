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

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            TabbedHomeScreen(
                viewModel = viewModel,
                onAddCredentialClick = { navController.navigate("add_edit/-1") },
                onCredentialClick = { id -> navController.navigate("add_edit/$id") },
                onAddBillClick = { navController.navigate("add_edit_bill/-1") },
                onBillClick = { id -> navController.navigate("add_edit_bill/$id") },
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
        composable(
            "add_edit_bill/{billId}",
            arguments = listOf(navArgument("billId") { type = NavType.IntType })
        ) { backStackEntry ->
            val billId = backStackEntry.arguments?.getInt("billId") ?: -1
            AddEditBillScreen(
                viewModel = viewModel,
                billId = billId,
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
