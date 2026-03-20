package com.yetzira.ContractorCashFlowAndroid.ui.navigation

import android.net.Uri
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.yetzira.ContractorCashFlowAndroid.ui.analytics.AnalyticsScreen
import com.yetzira.ContractorCashFlowAndroid.ui.clients.ClientDetailScreen
import com.yetzira.ContractorCashFlowAndroid.ui.clients.ClientRoutes
import com.yetzira.ContractorCashFlowAndroid.ui.clients.ClientViewModel
import com.yetzira.ContractorCashFlowAndroid.ui.clients.ClientViewModelFactory
import com.yetzira.ContractorCashFlowAndroid.ui.clients.ClientsListScreen
import com.yetzira.ContractorCashFlowAndroid.ui.clients.EditClientScreen
import com.yetzira.ContractorCashFlowAndroid.ui.clients.NewClientScreen
import com.yetzira.ContractorCashFlowAndroid.ui.expenses.EditExpenseScreen
import com.yetzira.ContractorCashFlowAndroid.ui.expenses.ExpenseRoutes
import com.yetzira.ContractorCashFlowAndroid.ui.expenses.ExpenseViewModel
import com.yetzira.ContractorCashFlowAndroid.ui.expenses.ExpenseViewModelFactory
import com.yetzira.ContractorCashFlowAndroid.ui.expenses.ExpensesListScreen
import com.yetzira.ContractorCashFlowAndroid.ui.expenses.NewExpenseScreen
import com.yetzira.ContractorCashFlowAndroid.ui.invoices.EditInvoiceScreen
import com.yetzira.ContractorCashFlowAndroid.ui.invoices.InvoiceRoutes
import com.yetzira.ContractorCashFlowAndroid.ui.invoices.InvoiceViewModel
import com.yetzira.ContractorCashFlowAndroid.ui.invoices.InvoiceViewModelFactory
import com.yetzira.ContractorCashFlowAndroid.ui.invoices.InvoicesListScreen
import com.yetzira.ContractorCashFlowAndroid.ui.invoices.NewInvoiceScreen
import com.yetzira.ContractorCashFlowAndroid.ui.labor.LaborScreen
import com.yetzira.ContractorCashFlowAndroid.ui.projects.EditProjectScreen
import com.yetzira.ContractorCashFlowAndroid.ui.projects.NewProjectScreen
import com.yetzira.ContractorCashFlowAndroid.ui.projects.ProjectDetailScreen
import com.yetzira.ContractorCashFlowAndroid.ui.projects.ProjectRoutes
import com.yetzira.ContractorCashFlowAndroid.ui.projects.ProjectViewModel
import com.yetzira.ContractorCashFlowAndroid.ui.projects.ProjectViewModelFactory
import com.yetzira.ContractorCashFlowAndroid.ui.projects.ProjectsListScreen
import com.yetzira.ContractorCashFlowAndroid.ui.settings.SettingsScreen
import androidx.navigation.navArgument
import com.yetzira.ContractorCashFlowAndroid.data.local.AppDatabase

fun NavGraphBuilder.projectsGraph(navController: NavController) {
    navigation(
        startDestination = ProjectRoutes.LIST,
        route = ProjectRoutes.GRAPH
    ) {
        composable(ProjectRoutes.LIST) {
            val context = LocalContext.current
            val factory = remember { ProjectViewModelFactory(AppDatabase.getInstance(context)) }
            val viewModel: ProjectViewModel = viewModel(factory = factory)

            ProjectsListScreen(
                viewModel = viewModel,
                onCreateProject = { navController.navigate(ProjectRoutes.NEW) },
                onOpenProject = { projectId -> navController.navigate(ProjectRoutes.detail(projectId)) }
            )
        }

        composable(ProjectRoutes.NEW) {
            val context = LocalContext.current
            val factory = remember { ProjectViewModelFactory(AppDatabase.getInstance(context)) }
            val viewModel: ProjectViewModel = viewModel(factory = factory)
            NewProjectScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = ProjectRoutes.DETAIL,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val context = LocalContext.current
            val factory = remember { ProjectViewModelFactory(AppDatabase.getInstance(context)) }
            val viewModel: ProjectViewModel = viewModel(factory = factory)
            val projectId = backStackEntry.arguments?.getString("projectId").orEmpty()

            ProjectDetailScreen(
                projectId = projectId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(ProjectRoutes.edit(id)) },
                onOpenClient = { clientName -> navController.navigate(ProjectRoutes.clientDetail(clientName)) }
            )
        }

        composable(
            route = ProjectRoutes.EDIT,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val context = LocalContext.current
            val factory = remember { ProjectViewModelFactory(AppDatabase.getInstance(context)) }
            val viewModel: ProjectViewModel = viewModel(factory = factory)
            val projectId = backStackEntry.arguments?.getString("projectId").orEmpty()
            EditProjectScreen(
                projectId = projectId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = ProjectRoutes.CLIENT_DETAIL,
            arguments = listOf(navArgument("clientName") { type = NavType.StringType })
        ) { backStackEntry ->
            val clientName = Uri.decode(backStackEntry.arguments?.getString("clientName").orEmpty())
            ClientDetailScreen(
                clientName = clientName,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

fun NavGraphBuilder.expensesGraph(navController: NavController) {
    navigation(
        startDestination = ExpenseRoutes.LIST,
        route = ExpenseRoutes.GRAPH
    ) {
        composable(ExpenseRoutes.LIST) {
            val context = LocalContext.current
            val factory = remember { ExpenseViewModelFactory(AppDatabase.getInstance(context)) }
            val viewModel: ExpenseViewModel = viewModel(factory = factory)

            ExpensesListScreen(
                viewModel = viewModel,
                onCreateExpense = { navController.navigate(ExpenseRoutes.NEW) },
                onEditExpense = { expenseId -> navController.navigate(ExpenseRoutes.edit(expenseId)) }
            )
        }

        composable(ExpenseRoutes.NEW) {
            val context = LocalContext.current
            val factory = remember { ExpenseViewModelFactory(AppDatabase.getInstance(context)) }
            val viewModel: ExpenseViewModel = viewModel(factory = factory)
            NewExpenseScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = ExpenseRoutes.EDIT,
            arguments = listOf(navArgument("expenseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val context = LocalContext.current
            val factory = remember { ExpenseViewModelFactory(AppDatabase.getInstance(context)) }
            val viewModel: ExpenseViewModel = viewModel(factory = factory)
            val expenseId = backStackEntry.arguments?.getString("expenseId").orEmpty()
            EditExpenseScreen(
                expenseId = expenseId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

fun NavGraphBuilder.invoicesGraph(navController: NavController) {
    navigation(
        startDestination = InvoiceRoutes.LIST,
        route = InvoiceRoutes.GRAPH
    ) {
        composable(InvoiceRoutes.LIST) {
            val context = LocalContext.current
            val factory = remember { InvoiceViewModelFactory(context, AppDatabase.getInstance(context)) }
            val viewModel: InvoiceViewModel = viewModel(factory = factory)

            InvoicesListScreen(
                viewModel = viewModel,
                onCreate = { navController.navigate(InvoiceRoutes.NEW) },
                onEdit = { invoiceId -> navController.navigate(InvoiceRoutes.edit(invoiceId)) }
            )
        }

        composable(InvoiceRoutes.NEW) {
            val context = LocalContext.current
            val factory = remember { InvoiceViewModelFactory(context, AppDatabase.getInstance(context)) }
            val viewModel: InvoiceViewModel = viewModel(factory = factory)
            NewInvoiceScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = InvoiceRoutes.EDIT,
            arguments = listOf(navArgument("invoiceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val context = LocalContext.current
            val factory = remember { InvoiceViewModelFactory(context, AppDatabase.getInstance(context)) }
            val viewModel: InvoiceViewModel = viewModel(factory = factory)
            val invoiceId = backStackEntry.arguments?.getString("invoiceId").orEmpty()

            EditInvoiceScreen(
                invoiceId = invoiceId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
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
        startDestination = ClientRoutes.LIST,
        route = ClientRoutes.GRAPH
    ) {
        composable(ClientRoutes.LIST) {
            val context = LocalContext.current
            val factory = remember { ClientViewModelFactory(AppDatabase.getInstance(context)) }
            val viewModel: ClientViewModel = viewModel(factory = factory)

            ClientsListScreen(
                viewModel = viewModel,
                onCreate = { navController.navigate(ClientRoutes.NEW) },
                onOpenDetail = { clientId -> navController.navigate(ClientRoutes.detail(clientId)) }
            )
        }

        composable(ClientRoutes.NEW) {
            val context = LocalContext.current
            val factory = remember { ClientViewModelFactory(AppDatabase.getInstance(context)) }
            val viewModel: ClientViewModel = viewModel(factory = factory)

            NewClientScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = ClientRoutes.DETAIL,
            arguments = listOf(navArgument("clientId") { type = NavType.StringType })
        ) { backStackEntry ->
            val context = LocalContext.current
            val factory = remember { ClientViewModelFactory(AppDatabase.getInstance(context)) }
            val viewModel: ClientViewModel = viewModel(factory = factory)
            val clientId = backStackEntry.arguments?.getString("clientId").orEmpty()

            ClientDetailScreen(
                clientId = clientId,
                viewModel = viewModel,
                onEdit = { id -> navController.navigate(ClientRoutes.edit(id)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = ClientRoutes.EDIT,
            arguments = listOf(navArgument("clientId") { type = NavType.StringType })
        ) { backStackEntry ->
            val context = LocalContext.current
            val factory = remember { ClientViewModelFactory(AppDatabase.getInstance(context)) }
            val viewModel: ClientViewModel = viewModel(factory = factory)
            val clientId = backStackEntry.arguments?.getString("clientId").orEmpty()

            EditClientScreen(
                clientId = clientId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
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

