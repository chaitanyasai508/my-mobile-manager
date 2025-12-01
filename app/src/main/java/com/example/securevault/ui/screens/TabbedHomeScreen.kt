package com.example.securevault.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.securevault.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabbedHomeScreen(
    viewModel: MainViewModel,
    onAddCredentialClick: () -> Unit,
    onCredentialClick: (Int) -> Unit,
    onAddBillClick: () -> Unit,
    onBillClick: (Int) -> Unit,
    onSettingsClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Passwords", "Bills")

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("SecureVault") },
                    actions = {
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                )
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> CredentialsListScreen(
                    viewModel = viewModel,
                    onAddClick = onAddCredentialClick,
                    onItemClick = onCredentialClick
                )
                1 -> BillsListScreen(
                    viewModel = viewModel,
                    onAddClick = onAddBillClick,
                    onItemClick = onBillClick
                )
            }
        }
    }
}
