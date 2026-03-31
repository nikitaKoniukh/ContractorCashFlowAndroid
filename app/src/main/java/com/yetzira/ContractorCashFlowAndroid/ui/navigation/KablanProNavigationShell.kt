package com.yetzira.ContractorCashFlowAndroid.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
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
    val backStackEntry by (navController as androidx.navigation.NavHostController).currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val screenTitle = stringResource(id = titleResForRoute(currentRoute))
    val hideShellTopBar = currentRoute in routesWithOwnTopBar

    androidx.compose.material3.Scaffold(
        topBar = {
            if (!hideShellTopBar) {
                KablanProTopBar(
                    title = screenTitle,
                    onMenuClick = onMenuClick
                )
            }
        }
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

@StringRes
private fun titleResForRoute(route: String?): Int {
    if (route == null) return com.yetzira.ContractorCashFlowAndroid.R.string.app_name

    return when {
        route == ProjectRoutes.LIST || route == ProjectRoutes.GRAPH -> com.yetzira.ContractorCashFlowAndroid.R.string.tab_projects
        route == ProjectRoutes.NEW -> com.yetzira.ContractorCashFlowAndroid.R.string.projects_new_project
        route == ProjectRoutes.DETAIL -> com.yetzira.ContractorCashFlowAndroid.R.string.projects_detail_title
        route == ProjectRoutes.EDIT -> com.yetzira.ContractorCashFlowAndroid.R.string.common_edit
        route == ProjectRoutes.CLIENT_DETAIL -> com.yetzira.ContractorCashFlowAndroid.R.string.clients_detail_title

        route == ExpenseRoutes.LIST || route == ExpenseRoutes.GRAPH -> com.yetzira.ContractorCashFlowAndroid.R.string.tab_expenses
        route == ExpenseRoutes.NEW -> com.yetzira.ContractorCashFlowAndroid.R.string.expenses_new
        route == ExpenseRoutes.EDIT -> com.yetzira.ContractorCashFlowAndroid.R.string.expenses_edit

        route == InvoiceRoutes.LIST || route == InvoiceRoutes.GRAPH -> com.yetzira.ContractorCashFlowAndroid.R.string.tab_invoices
        route == InvoiceRoutes.NEW -> com.yetzira.ContractorCashFlowAndroid.R.string.invoices_new
        route == InvoiceRoutes.EDIT -> com.yetzira.ContractorCashFlowAndroid.R.string.invoices_edit

        route == LaborRoutes.LIST || route == LaborRoutes.GRAPH -> com.yetzira.ContractorCashFlowAndroid.R.string.tab_labor
        route == LaborRoutes.ADD -> com.yetzira.ContractorCashFlowAndroid.R.string.labor_screen_add_title
        route == LaborRoutes.EDIT -> com.yetzira.ContractorCashFlowAndroid.R.string.labor_screen_edit_title

        route == ClientRoutes.LIST || route == ClientRoutes.GRAPH -> com.yetzira.ContractorCashFlowAndroid.R.string.tab_clients
        route == ClientRoutes.NEW -> com.yetzira.ContractorCashFlowAndroid.R.string.clients_new
        route == ClientRoutes.DETAIL -> com.yetzira.ContractorCashFlowAndroid.R.string.clients_detail_title
        route == ClientRoutes.EDIT -> com.yetzira.ContractorCashFlowAndroid.R.string.clients_edit

        route == TabDestination.ANALYTICS.route || route == "analytics_graph" -> com.yetzira.ContractorCashFlowAndroid.R.string.tab_analytics

        route == SettingsRoutes.ROOT || route == SettingsRoutes.GRAPH -> com.yetzira.ContractorCashFlowAndroid.R.string.tab_settings
        route == SettingsRoutes.PAYWALL -> com.yetzira.ContractorCashFlowAndroid.R.string.settings_upgrade_pro

        else -> com.yetzira.ContractorCashFlowAndroid.R.string.app_name
    }
}

/** Routes where the screen provides its own TopAppBar (back / save / menu). */
private val routesWithOwnTopBar = setOf(
    ProjectRoutes.DETAIL,
    ProjectRoutes.NEW,
    ProjectRoutes.EDIT,
    ProjectRoutes.CLIENT_DETAIL,
    ExpenseRoutes.NEW,
    ExpenseRoutes.EDIT,
    InvoiceRoutes.NEW,
    InvoiceRoutes.EDIT,
    LaborRoutes.ADD,
    LaborRoutes.EDIT,
    ClientRoutes.NEW,
    ClientRoutes.DETAIL,
    ClientRoutes.EDIT,
)

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

