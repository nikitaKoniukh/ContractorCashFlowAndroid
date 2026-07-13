package com.yetzira.ContractorCashFlowAndroid.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavigationRoute(val route: String) {
    object Projects : NavigationRoute("projects")
    object Expenses : NavigationRoute("expenses")
    object Invoices : NavigationRoute("invoices")
    object Labor : NavigationRoute("labor")
    object Clients : NavigationRoute("clients")
    object Analytics : NavigationRoute("analytics")
    object Settings : NavigationRoute("settings")
}

enum class TabDestination(
    val route: String,
    val label: Int,
    /** Filled icon — shown when this tab is active. */
    val icon: ImageVector,
    /** Outlined icon — shown when this tab is inactive. */
    val inactiveIcon: ImageVector,
    val description: Int
) {
    PROJECTS(
        route = "projects",
        label = com.yetzira.ContractorCashFlowAndroid.R.string.tab_projects,
        icon = Icons.Filled.Folder,
        inactiveIcon = Icons.Outlined.Folder,
        description = com.yetzira.ContractorCashFlowAndroid.R.string.tab_projects_description
    ),
    EXPENSES(
        route = "expenses",
        label = com.yetzira.ContractorCashFlowAndroid.R.string.tab_expenses,
        icon = Icons.Filled.AttachMoney,
        inactiveIcon = Icons.Outlined.AttachMoney,
        description = com.yetzira.ContractorCashFlowAndroid.R.string.tab_expenses_description
    ),
    INVOICES(
        route = "invoices",
        label = com.yetzira.ContractorCashFlowAndroid.R.string.tab_invoices,
        icon = Icons.Filled.Description,
        inactiveIcon = Icons.Outlined.Description,
        description = com.yetzira.ContractorCashFlowAndroid.R.string.tab_invoices_description
    ),
    LABOR(
        route = "labor",
        label = com.yetzira.ContractorCashFlowAndroid.R.string.tab_labor,
        icon = Icons.Filled.Groups,
        inactiveIcon = Icons.Outlined.Groups,
        description = com.yetzira.ContractorCashFlowAndroid.R.string.tab_labor_description
    ),
    CLIENTS(
        route = "clients",
        label = com.yetzira.ContractorCashFlowAndroid.R.string.tab_clients,
        icon = Icons.Filled.People,
        inactiveIcon = Icons.Outlined.People,
        description = com.yetzira.ContractorCashFlowAndroid.R.string.tab_clients_description
    ),
    ANALYTICS(
        route = "analytics",
        label = com.yetzira.ContractorCashFlowAndroid.R.string.tab_analytics,
        icon = Icons.Filled.BarChart,
        inactiveIcon = Icons.Outlined.BarChart,
        description = com.yetzira.ContractorCashFlowAndroid.R.string.tab_analytics_description
    ),
    SETTINGS(
        route = "settings",
        label = com.yetzira.ContractorCashFlowAndroid.R.string.tab_settings,
        icon = Icons.Filled.Settings,
        inactiveIcon = Icons.Outlined.Settings,
        description = com.yetzira.ContractorCashFlowAndroid.R.string.tab_settings_description
    );

    companion object {
        fun fromRoute(route: String?): TabDestination? =
            entries.find { it.route == route }

        /** All 7 tabs shown in the bottom bar (matches iOS). */
        val bottomBarTabs = listOf(
            PROJECTS, EXPENSES, INVOICES, LABOR, CLIENTS, ANALYTICS, SETTINGS
        )
    }
}
