package com.yetzira.ContractorCashFlowAndroid.ui.navigation

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yetzira.ContractorCashFlowAndroid.ui.clients.ClientRoutes
import com.yetzira.ContractorCashFlowAndroid.ui.expenses.ExpenseRoutes
import com.yetzira.ContractorCashFlowAndroid.ui.invoices.InvoiceRoutes
import com.yetzira.ContractorCashFlowAndroid.ui.labor.LaborRoutes
import com.yetzira.ContractorCashFlowAndroid.ui.projects.ProjectRoutes
import com.yetzira.ContractorCashFlowAndroid.ui.settings.SettingsRoutes

@Composable
fun KablanProNavigationShell(
    selectedTab: MutableState<TabDestination>,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute !in routesWithOwnTopBar
    var showMoreSheet by remember { mutableStateOf(false) }

    fun navigate(tab: TabDestination) {
        if (tab == selectedTab.value) {
            navController.popBackStack(getTabRootRoute(tab), inclusive = false)
        } else {
            selectedTab.value = tab
            navController.navigate(getGraphRoute(tab)) {
                popUpTo(navController.graph.startDestinationId) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Scaffold(
        contentWindowInsets = KablanProLayoutDefaults.ScaffoldContentInsets,
        bottomBar = {
            if (showBottomBar) {
                KablanProNavigationBar(
                    selectedTab = selectedTab.value,
                    onTabSelected = { tab ->
                        if (tab == TabDestination.MORE) {
                            showMoreSheet = true
                        } else {
                            navigate(tab)
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        KablanProNavigationContent(
            navController = navController,
            modifier = modifier.padding(innerPadding)
        )
    }

    if (showMoreSheet) {
        MoreBottomSheet(
            selectedTab = selectedTab.value,
            onTabSelected = { tab -> navigate(tab) },
            onDismiss = { showMoreSheet = false }
        )
    }
}

@Composable
private fun KablanProNavigationContent(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val screenTitle = stringResource(id = titleResForRoute(currentRoute))
    val hideShellTopBar = currentRoute in routesWithOwnTopBar
    val isModal = hideShellTopBar

    Scaffold(
        contentWindowInsets = KablanProLayoutDefaults.ScaffoldContentInsets,
        topBar = {
            // Fade the shell top bar out over the same duration as the modal slides up,
            // so there's no abrupt disappear flash during modal presentation.
            AnimatedVisibility(
                visible = !hideShellTopBar,
                enter = fadeIn(tween(120)),
                exit = fadeOut(tween(340))
            ) {
                KablanProTopBar(title = screenTitle)
            }
        }
    ) { paddingValues ->
        // Swipe-down-to-dismiss state (accumulated drag)
        val dragAccum = remember { mutableFloatStateOf(0f) }

        val contentModifier = if (isModal) {
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pointerInput(currentRoute) {
                    detectVerticalDragGestures(
                        onDragStart = { dragAccum.floatValue = 0f },
                        onDragEnd = {
                            if (dragAccum.floatValue > 120.dp.toPx()) {
                                navController.popBackStack()
                            }
                            dragAccum.floatValue = 0f
                        },
                        onVerticalDrag = { change, amount ->
                            if (amount > 0) {
                                change.consume()
                                dragAccum.floatValue += amount
                            }
                        }
                    )
                }
        } else {
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        }

        Box(modifier = modifier.then(contentModifier)) {
            NavHost(
                navController = navController,
                startDestination = ProjectRoutes.GRAPH,
                modifier = Modifier.fillMaxSize(),
                enterTransition = {
                    val from = initialState.destination.route
                    val to = targetState.destination.route
                    when {
                        isPresentingModal(from, to) -> slideInVertically(
                            animationSpec = tween(340),
                            initialOffsetY = { it }
                        )
                        isTopBarOwnerChanging(from, to) -> EnterTransition.None
                        else -> fadeIn(tween(300))
                    }
                },
                exitTransition = {
                    val from = initialState.destination.route
                    val to = targetState.destination.route
                    when {
                        isPresentingModal(from, to) -> fadeOut(tween(200))
                        isTopBarOwnerChanging(from, to) -> ExitTransition.None
                        else -> fadeOut(tween(300))
                    }
                },
                popEnterTransition = {
                    val from = initialState.destination.route
                    val to = targetState.destination.route
                    when {
                        isDismissingModal(from, to) -> fadeIn(tween(220))
                        isTopBarOwnerChanging(from, to) -> EnterTransition.None
                        else -> fadeIn(tween(220))
                    }
                },
                popExitTransition = {
                    val from = initialState.destination.route
                    val to = targetState.destination.route
                    when {
                        isDismissingModal(from, to) -> slideOutVertically(
                            animationSpec = tween(300),
                            targetOffsetY = { it }
                        )
                        isTopBarOwnerChanging(from, to) -> ExitTransition.None
                        else -> fadeOut(tween(220))
                    }
                }
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
        route == ExpenseRoutes.DETAIL -> com.yetzira.ContractorCashFlowAndroid.R.string.expenses_section_details
        route == ExpenseRoutes.EDIT -> com.yetzira.ContractorCashFlowAndroid.R.string.expenses_edit

        route == InvoiceRoutes.LIST || route == InvoiceRoutes.GRAPH -> com.yetzira.ContractorCashFlowAndroid.R.string.tab_invoices
        route == InvoiceRoutes.NEW -> com.yetzira.ContractorCashFlowAndroid.R.string.invoices_new
        route == InvoiceRoutes.DETAIL -> com.yetzira.ContractorCashFlowAndroid.R.string.invoices_section_details
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
    ExpenseRoutes.DETAIL,
    ExpenseRoutes.EDIT,
    InvoiceRoutes.NEW,
    InvoiceRoutes.DETAIL,
    InvoiceRoutes.EDIT,
    LaborRoutes.ADD,
    LaborRoutes.EDIT,
    ClientRoutes.NEW,
    ClientRoutes.DETAIL,
    ClientRoutes.EDIT,
)

private fun isTopBarOwnerChanging(fromRoute: String?, toRoute: String?): Boolean {
    val fromOwnTopBar = fromRoute in routesWithOwnTopBar
    val toOwnTopBar = toRoute in routesWithOwnTopBar
    return fromOwnTopBar != toOwnTopBar
}

private fun isPresentingModal(fromRoute: String?, toRoute: String?): Boolean =
    fromRoute !in routesWithOwnTopBar && toRoute in routesWithOwnTopBar

private fun isDismissingModal(fromRoute: String?, toRoute: String?): Boolean =
    fromRoute in routesWithOwnTopBar && toRoute !in routesWithOwnTopBar

private fun getGraphRoute(tab: TabDestination): String = when (tab) {
    TabDestination.PROJECTS -> ProjectRoutes.GRAPH
    TabDestination.EXPENSES -> ExpenseRoutes.GRAPH
    TabDestination.INVOICES -> InvoiceRoutes.GRAPH
    TabDestination.LABOR -> LaborRoutes.GRAPH
    TabDestination.CLIENTS -> ClientRoutes.GRAPH
    TabDestination.ANALYTICS -> "analytics_graph"
    TabDestination.SETTINGS -> SettingsRoutes.GRAPH
    TabDestination.MORE -> ProjectRoutes.GRAPH
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

