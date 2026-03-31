package com.yetzira.ContractorCashFlowAndroid.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
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
    val icon: ImageVector,
    val description: Int
) {
    PROJECTS(
        route = "projects",
        label = com.yetzira.ContractorCashFlowAndroid.R.string.tab_projects,
        icon = Icons.Default.Folder,
        description = com.yetzira.ContractorCashFlowAndroid.R.string.tab_projects_description
    ),
    EXPENSES(
        route = "expenses",
        label = com.yetzira.ContractorCashFlowAndroid.R.string.tab_expenses,
        icon = Icons.Default.AttachMoney,
        description = com.yetzira.ContractorCashFlowAndroid.R.string.tab_expenses_description
    ),
    INVOICES(
        route = "invoices",
        label = com.yetzira.ContractorCashFlowAndroid.R.string.tab_invoices,
        icon = Icons.Default.Description,
        description = com.yetzira.ContractorCashFlowAndroid.R.string.tab_invoices_description
    ),
    LABOR(
        route = "labor",
        label = com.yetzira.ContractorCashFlowAndroid.R.string.tab_labor,
        icon = Icons.Default.Groups,
        description = com.yetzira.ContractorCashFlowAndroid.R.string.tab_labor_description
    ),
    CLIENTS(
        route = "clients",
        label = com.yetzira.ContractorCashFlowAndroid.R.string.tab_clients,
        icon = Icons.Default.People,
        description = com.yetzira.ContractorCashFlowAndroid.R.string.tab_clients_description
    ),
    ANALYTICS(
        route = "analytics",
        label = com.yetzira.ContractorCashFlowAndroid.R.string.tab_analytics,
        icon = Icons.Default.BarChart,
        description = com.yetzira.ContractorCashFlowAndroid.R.string.tab_analytics_description
    ),
    SETTINGS(
        route = "settings",
        label = com.yetzira.ContractorCashFlowAndroid.R.string.tab_settings,
        icon = Icons.Default.Settings,
        description = com.yetzira.ContractorCashFlowAndroid.R.string.tab_settings_description
    );

    companion object {
        fun fromRoute(route: String?): TabDestination? =
            entries.find { it.route == route }
    }
}

