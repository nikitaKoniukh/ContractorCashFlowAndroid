package com.yetzira.ContractorCashFlowAndroid.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.yetzira.ContractorCashFlowAndroid.ui.clients.ClientRoutes
import com.yetzira.ContractorCashFlowAndroid.ui.expenses.ExpenseRoutes
import com.yetzira.ContractorCashFlowAndroid.ui.invoices.InvoiceRoutes
import com.yetzira.ContractorCashFlowAndroid.ui.labor.LaborRoutes
import com.yetzira.ContractorCashFlowAndroid.ui.projects.ProjectRoutes
import com.yetzira.ContractorCashFlowAndroid.ui.settings.SettingsRoutes
import kotlinx.coroutines.launch

@Composable
fun KablanProNavigationShell(
    selectedTab: MutableState<TabDestination>,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            KablanProNavigationDrawer(
                selectedTab = selectedTab.value,
                onTabSelected = { newTab: TabDestination ->
                    coroutineScope.launch {
                        drawerState.close()
                    }
                    if (newTab == selectedTab.value) {
                        navController.popBackStack(getTabRootRoute(newTab), inclusive = false)
                    } else {
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
    ) {
        KablanProNavigationContent(
            navController = navController,
            onMenuClick = {
                coroutineScope.launch {
                    drawerState.open()
                }
            },
            modifier = modifier
        )
    }
}

@Composable
private fun KablanProNavigationContent(
    navController: NavController,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Scaffold(
        topBar = { KablanProTopBar(onMenuClick = onMenuClick) }
    ) { paddingValues ->
        androidx.compose.foundation.layout.Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NavHost(
                navController = navController as androidx.navigation.NavHostController,
                startDestination = ProjectRoutes.GRAPH,
                modifier = Modifier.fillMaxSize()
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
}

private fun getGraphRoute(tab: TabDestination): String = when (tab) {
    TabDestination.PROJECTS -> ProjectRoutes.GRAPH
    TabDestination.EXPENSES -> ExpenseRoutes.GRAPH
    TabDestination.INVOICES -> InvoiceRoutes.GRAPH
    TabDestination.LABOR -> LaborRoutes.GRAPH
    TabDestination.CLIENTS -> ClientRoutes.GRAPH
    TabDestination.ANALYTICS -> "analytics_graph"
    TabDestination.SETTINGS -> SettingsRoutes.GRAPH
}

private fun getTabRootRoute(tab: TabDestination): String = when (tab) {
    TabDestination.PROJECTS -> ProjectRoutes.LIST
    TabDestination.EXPENSES -> ExpenseRoutes.LIST
    TabDestination.INVOICES -> InvoiceRoutes.LIST
    TabDestination.LABOR -> LaborRoutes.LIST
    TabDestination.CLIENTS -> ClientRoutes.LIST
    TabDestination.SETTINGS -> SettingsRoutes.ROOT
    else -> tab.route
}

