package com.yetzira.ContractorCashFlowAndroid.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.yetzira.ContractorCashFlowAndroid.ui.analytics.AnalyticsScreen
import com.yetzira.ContractorCashFlowAndroid.ui.clients.ClientsScreen
import com.yetzira.ContractorCashFlowAndroid.ui.expenses.ExpensesScreen
import com.yetzira.ContractorCashFlowAndroid.ui.invoices.InvoicesScreen
import com.yetzira.ContractorCashFlowAndroid.ui.labor.LaborScreen
import com.yetzira.ContractorCashFlowAndroid.ui.projects.ProjectsScreen
import com.yetzira.ContractorCashFlowAndroid.ui.settings.SettingsScreen

fun NavGraphBuilder.projectsGraph(navController: NavController) {
    navigation(
        startDestination = TabDestination.PROJECTS.route,
        route = "projects_graph"
    ) {
        composable(TabDestination.PROJECTS.route) {
            ProjectsScreen()
        }
    }
}

fun NavGraphBuilder.expensesGraph(navController: NavController) {
    navigation(
        startDestination = TabDestination.EXPENSES.route,
        route = "expenses_graph"
    ) {
        composable(TabDestination.EXPENSES.route) {
            ExpensesScreen()
        }
    }
}

fun NavGraphBuilder.invoicesGraph(navController: NavController) {
    navigation(
        startDestination = TabDestination.INVOICES.route,
        route = "invoices_graph"
    ) {
        composable(TabDestination.INVOICES.route) {
            InvoicesScreen()
        }
    }
}

fun NavGraphBuilder.laborGraph(navController: NavController) {
    navigation(
        startDestination = TabDestination.LABOR.route,
        route = "labor_graph"
    ) {
        composable(TabDestination.LABOR.route) {
            LaborScreen()
        }
    }
}

fun NavGraphBuilder.clientsGraph(navController: NavController) {
    navigation(
        startDestination = TabDestination.CLIENTS.route,
        route = "clients_graph"
    ) {
        composable(TabDestination.CLIENTS.route) {
            ClientsScreen()
        }
    }
}

fun NavGraphBuilder.analyticsGraph(navController: NavController) {
    navigation(
        startDestination = TabDestination.ANALYTICS.route,
        route = "analytics_graph"
    ) {
        composable(TabDestination.ANALYTICS.route) {
            AnalyticsScreen()
        }
    }
}

fun NavGraphBuilder.settingsGraph(navController: NavController) {
    navigation(
        startDestination = TabDestination.SETTINGS.route,
        route = "settings_graph"
    ) {
        composable(TabDestination.SETTINGS.route) {
            SettingsScreen()
        }
    }
}

