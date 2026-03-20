package com.yetzira.ContractorCashFlowAndroid.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

@Composable
fun KablanProNavigationShell(
    selectedTab: MutableState<TabDestination>,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            KablanProNavigationBar(
                selectedTab = selectedTab.value,
                onTabSelected = { newTab ->
                    if (newTab == selectedTab.value) {
                        // Pop to root if tapping same tab
                        navController.popBackStack(newTab.route, inclusive = false)
                    } else {
                        // Navigate to new tab, clearing back stack
                        selectedTab.value = newTab
                        navController.navigate(getGraphRoute(newTab)) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "projects_graph",
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            projectsGraph(navController)
            expensesGraph(navController)
            invoicesGraph(navController)
            laborGraph(navController)
            clientsGraph(navController)
            analyticsGraph(navController)
            settingsGraph(navController)
        }
    }
}

private fun getGraphRoute(tab: TabDestination): String = when (tab) {
    TabDestination.PROJECTS -> "projects_graph"
    TabDestination.EXPENSES -> "expenses_graph"
    TabDestination.INVOICES -> "invoices_graph"
    TabDestination.LABOR -> "labor_graph"
    TabDestination.CLIENTS -> "clients_graph"
    TabDestination.ANALYTICS -> "analytics_graph"
    TabDestination.SETTINGS -> "settings_graph"
}

