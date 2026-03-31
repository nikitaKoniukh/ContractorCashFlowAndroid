package com.yetzira.ContractorCashFlowAndroid.ui.navigation

import android.net.Uri
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.yetzira.ContractorCashFlowAndroid.ui.scan.ScanExpenseScreen
import com.yetzira.ContractorCashFlowAndroid.ui.scan.ScannedExpenseReviewScreen
import com.yetzira.ContractorCashFlowAndroid.ui.analytics.AnalyticsScreen
import com.yetzira.ContractorCashFlowAndroid.ui.analytics.AnalyticsViewModel
import com.yetzira.ContractorCashFlowAndroid.ui.analytics.AnalyticsViewModelFactory
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
import com.yetzira.ContractorCashFlowAndroid.ui.labor.AddLaborScreen
import com.yetzira.ContractorCashFlowAndroid.ui.labor.EditLaborScreen
import com.yetzira.ContractorCashFlowAndroid.ui.labor.LaborListScreen
import com.yetzira.ContractorCashFlowAndroid.ui.labor.LaborRoutes
import com.yetzira.ContractorCashFlowAndroid.ui.labor.LaborViewModel
import com.yetzira.ContractorCashFlowAndroid.ui.labor.LaborViewModelFactory
import com.yetzira.ContractorCashFlowAndroid.ui.projects.EditProjectScreen
import com.yetzira.ContractorCashFlowAndroid.ui.projects.NewProjectScreen
import com.yetzira.ContractorCashFlowAndroid.ui.projects.ProjectDetailScreen
import com.yetzira.ContractorCashFlowAndroid.ui.projects.ProjectRoutes
import com.yetzira.ContractorCashFlowAndroid.ui.projects.ProjectViewModel
import com.yetzira.ContractorCashFlowAndroid.ui.projects.ProjectViewModelFactory
import com.yetzira.ContractorCashFlowAndroid.ui.projects.ProjectsListScreen
import com.yetzira.ContractorCashFlowAndroid.ui.paywall.PaywallScreen
import com.yetzira.ContractorCashFlowAndroid.billing.PurchaseViewModel
import com.yetzira.ContractorCashFlowAndroid.billing.PurchaseViewModelFactory
import com.yetzira.ContractorCashFlowAndroid.ui.settings.SettingsScreen
import com.yetzira.ContractorCashFlowAndroid.ui.settings.SettingsRoutes
import com.yetzira.ContractorCashFlowAndroid.ui.settings.SettingsViewModel
import com.yetzira.ContractorCashFlowAndroid.ui.settings.SettingsViewModelFactory
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
                onOpenClient = { clientName -> navController.navigate(ProjectRoutes.clientDetail(clientName)) },
                onAddExpense = { navController.navigate(ExpenseRoutes.NEW) },
                onAddInvoice = { navController.navigate(InvoiceRoutes.NEW) },
                onOpenExpense = { expenseId -> navController.navigate(ExpenseRoutes.edit(expenseId)) },
                onOpenInvoice = { invoiceId -> navController.navigate(InvoiceRoutes.edit(invoiceId)) }
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
            val context = LocalContext.current
            val factory = remember { ClientViewModelFactory(com.yetzira.ContractorCashFlowAndroid.data.local.AppDatabase.getInstance(context)) }
            val clientsViewModel: ClientViewModel = viewModel(factory = factory)
            val clientName = Uri.decode(backStackEntry.arguments?.getString("clientName").orEmpty())
            val listState by clientsViewModel.listUiState.collectAsState()
            val matchedClient = listState.clients.firstOrNull { it.name.equals(clientName, ignoreCase = true) }
            ClientDetailScreen(
                clientId = matchedClient?.id ?: "",
                viewModel = clientsViewModel,
                onEdit = { id -> navController.navigate(ClientRoutes.edit(id)) },
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
                onEditExpense = { expenseId -> navController.navigate(ExpenseRoutes.edit(expenseId)) },
                onScanReceipt = { navController.navigate(ExpenseRoutes.SCAN) }
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

        composable(ExpenseRoutes.SCAN) {
            ScanExpenseScreen(
                onImageCaptured = { uri ->
                    val encodedUri = Uri.encode(uri.toString())
                    navController.navigate(ExpenseRoutes.scanReview(encodedUri))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = ExpenseRoutes.SCAN_REVIEW,
            arguments = listOf(navArgument("imageUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val context = LocalContext.current
            val db = remember { AppDatabase.getInstance(context) }
            val imageUriStr = Uri.decode(backStackEntry.arguments?.getString("imageUri").orEmpty())
            val imageUri = Uri.parse(imageUriStr)
            val activeProjects by db.projectDao().getAll().collectAsState(initial = emptyList())

            ScannedExpenseReviewScreen(
                imageUri = imageUri,
                activeProjects = activeProjects.filter { it.isActive },
                onSave = { expense ->
                    kotlinx.coroutines.MainScope().launch {
                        db.expenseDao().insert(expense)
                        navController.popBackStack(ExpenseRoutes.LIST, inclusive = false)
                    }
                },
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
        startDestination = LaborRoutes.LIST,
        route = LaborRoutes.GRAPH
    ) {
        composable(LaborRoutes.LIST) {
            val context = LocalContext.current
            val factory = remember { LaborViewModelFactory(AppDatabase.getInstance(context)) }
            val viewModel: LaborViewModel = viewModel(factory = factory)

            LaborListScreen(
                viewModel = viewModel,
                onAdd = { navController.navigate(LaborRoutes.ADD) },
                onEdit = { workerId -> navController.navigate(LaborRoutes.edit(workerId)) }
            )
        }

        composable(LaborRoutes.ADD) {
            val context = LocalContext.current
            val factory = remember { LaborViewModelFactory(AppDatabase.getInstance(context)) }
            val viewModel: LaborViewModel = viewModel(factory = factory)
            AddLaborScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = LaborRoutes.EDIT,
            arguments = listOf(navArgument("workerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val context = LocalContext.current
            val factory = remember { LaborViewModelFactory(AppDatabase.getInstance(context)) }
            val viewModel: LaborViewModel = viewModel(factory = factory)
            val workerId = backStackEntry.arguments?.getString("workerId").orEmpty()

            EditLaborScreen(
                workerId = workerId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
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

fun NavGraphBuilder.analyticsGraph(@Suppress("UNUSED_PARAMETER") navController: NavController) {
    navigation(
        startDestination = TabDestination.ANALYTICS.route,
        route = "analytics_graph"
    ) {
        composable(TabDestination.ANALYTICS.route) {
            val context = LocalContext.current
            val factory = remember { AnalyticsViewModelFactory(context, AppDatabase.getInstance(context)) }
            val viewModel: AnalyticsViewModel = viewModel(factory = factory)

            AnalyticsScreen(viewModel = viewModel)
        }
    }
}

fun NavGraphBuilder.settingsGraph(navController: NavController) {
    navigation(
        startDestination = SettingsRoutes.ROOT,
        route = SettingsRoutes.GRAPH
    ) {
        composable(SettingsRoutes.ROOT) {
            val context = LocalContext.current
            val factory = remember { SettingsViewModelFactory(context, AppDatabase.getInstance(context)) }
            val viewModel: SettingsViewModel = viewModel(factory = factory)

            SettingsScreen(
                viewModel = viewModel,
                onOpenPaywall = { navController.navigate(SettingsRoutes.PAYWALL) }
            )
        }

        composable(SettingsRoutes.PAYWALL) {
            val context = LocalContext.current
            val factory = remember { PurchaseViewModelFactory(context) }
            val viewModel: PurchaseViewModel = viewModel(factory = factory)

            PaywallScreen(
                viewModel = viewModel,
                onDismiss = { navController.popBackStack() }
            )
        }
    }
}

