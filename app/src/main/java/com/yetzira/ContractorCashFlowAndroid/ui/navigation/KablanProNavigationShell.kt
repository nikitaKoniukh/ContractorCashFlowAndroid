package com.yetzira.ContractorCashFlowAndroid.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.yetzira.ContractorCashFlowAndroid.ui.clients.ClientRoutes
import com.yetzira.ContractorCashFlowAndroid.ui.expenses.ExpenseRoutes
import com.yetzira.ContractorCashFlowAndroid.ui.invoices.InvoiceRoutes
import com.yetzira.ContractorCashFlowAndroid.ui.projects.ProjectRoutes

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
                        navController.popBackStack(getTabRootRoute(newTab), inclusive = false)
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
            startDestination = ProjectRoutes.GRAPH,
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
    TabDestination.PROJECTS -> ProjectRoutes.GRAPH
    TabDestination.EXPENSES -> ExpenseRoutes.GRAPH
    TabDestination.INVOICES -> InvoiceRoutes.GRAPH
    TabDestination.LABOR -> "labor_graph"
    TabDestination.CLIENTS -> ClientRoutes.GRAPH
    TabDestination.ANALYTICS -> "analytics_graph"
    TabDestination.SETTINGS -> "settings_graph"
}

private fun getTabRootRoute(tab: TabDestination): String = when (tab) {
    TabDestination.PROJECTS -> ProjectRoutes.LIST
    TabDestination.EXPENSES -> ExpenseRoutes.LIST
    TabDestination.INVOICES -> InvoiceRoutes.LIST
    TabDestination.CLIENTS -> ClientRoutes.LIST
    else -> tab.route
}

