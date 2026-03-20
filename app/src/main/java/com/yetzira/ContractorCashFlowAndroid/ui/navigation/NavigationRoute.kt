package com.yetzira.ContractorCashFlowAndroid.ui.navigation

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
    val icon: Int,
    val description: Int
) {
    PROJECTS(
        route = "projects",
        label = com.yetzira.ContractorCashFlowAndroid.R.string.tab_projects,
        icon = android.R.drawable.ic_dialog_info,
        description = com.yetzira.ContractorCashFlowAndroid.R.string.tab_projects_description
    ),
    EXPENSES(
        route = "expenses",
        label = com.yetzira.ContractorCashFlowAndroid.R.string.tab_expenses,
        icon = android.R.drawable.ic_dialog_info,
        description = com.yetzira.ContractorCashFlowAndroid.R.string.tab_expenses_description
    ),
    INVOICES(
        route = "invoices",
        label = com.yetzira.ContractorCashFlowAndroid.R.string.tab_invoices,
        icon = android.R.drawable.ic_dialog_info,
        description = com.yetzira.ContractorCashFlowAndroid.R.string.tab_invoices_description
    ),
    LABOR(
        route = "labor",
        label = com.yetzira.ContractorCashFlowAndroid.R.string.tab_labor,
        icon = android.R.drawable.ic_dialog_info,
        description = com.yetzira.ContractorCashFlowAndroid.R.string.tab_labor_description
    ),
    CLIENTS(
        route = "clients",
        label = com.yetzira.ContractorCashFlowAndroid.R.string.tab_clients,
        icon = android.R.drawable.ic_dialog_info,
        description = com.yetzira.ContractorCashFlowAndroid.R.string.tab_clients_description
    ),
    ANALYTICS(
        route = "analytics",
        label = com.yetzira.ContractorCashFlowAndroid.R.string.tab_analytics,
        icon = android.R.drawable.ic_dialog_info,
        description = com.yetzira.ContractorCashFlowAndroid.R.string.tab_analytics_description
    ),
    SETTINGS(
        route = "settings",
        label = com.yetzira.ContractorCashFlowAndroid.R.string.tab_settings,
        icon = android.R.drawable.ic_menu_preferences,
        description = com.yetzira.ContractorCashFlowAndroid.R.string.tab_settings_description
    );

    companion object {
        fun fromRoute(route: String?): TabDestination? =
            values().find { it.route == route }
    }
}

