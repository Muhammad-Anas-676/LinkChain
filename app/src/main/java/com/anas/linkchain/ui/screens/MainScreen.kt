package com.anas.linkchain.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anas.linkchain.ui.theme.DarkAccent
import com.anas.linkchain.ui.theme.DarkSurface
import com.anas.linkchain.ui.viewmodel.HistoryViewModel
import com.anas.linkchain.ui.viewmodel.QueueViewModel
import com.anas.linkchain.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    queueViewModel: QueueViewModel = viewModel(),
    historyViewModel: HistoryViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = DarkSurface) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.List, contentDescription = "Queue") },
                    label = { Text("Queue") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = DarkAccent, indicatorColor = DarkSurface)
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = DarkAccent, indicatorColor = DarkSurface)
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = DarkAccent, indicatorColor = DarkSurface)
                )
            }
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> QueueScreen(viewModel = queueViewModel)
                1 -> SettingsScreen(viewModel = settingsViewModel)
                2 -> HistoryScreen(viewModel = historyViewModel)
            }
        }
    }
}