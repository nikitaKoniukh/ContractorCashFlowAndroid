package com.yetzira.ContractorCashFlowAndroid.ui.projects

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.InvoiceEntity
import com.yetzira.ContractorCashFlowAndroid.data.preferences.CurrencyOption
import com.yetzira.ContractorCashFlowAndroid.data.preferences.UserPreferencesRepository
import com.yetzira.ContractorCashFlowAndroid.export.ProjectExportService
import com.yetzira.ContractorCashFlowAndroid.ui.components.formatCurrencyAmount
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    projectId: String,
    viewModel: ProjectViewModel,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onOpenClient: (String) -> Unit,
    modifier: Modifier = Modifier,
    onAddExpense: () -> Unit = {},
    onAddInvoice: () -> Unit = {},
    onOpenExpense: (String) -> Unit = {},
    onOpenInvoice: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val preferencesRepository = remember(context) { UserPreferencesRepository(context.applicationContext) }
    val currency by preferencesRepository.selectedCurrencyCode.collectAsState(initial = CurrencyOption.ILS)
    val state by viewModel.detailUiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showMenu by remember { mutableStateOf(false) }
    var showExportSheet by remember { mutableStateOf(false) }
    var includeExpensesInExport by remember { mutableStateOf(true) }
    var includeInvoicesInExport by remember { mutableStateOf(true) }
    val projectExportService = remember { ProjectExportService() }
    val expenseDeletedMessage = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_expense_deleted)
    val invoiceDeletedMessage = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_invoice_deleted)
    val undoLabel = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.common_undo)

    LaunchedEffect(projectId) {
        viewModel.selectProject(projectId)
    }

    Scaffold(
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.project?.name ?: "",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.common_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.common_more)
                        )
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.common_edit)) },
                            onClick = {
                                showMenu = false
                                state.project?.let { onEdit(it.id) }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_export)) },
                            onClick = {
                                showMenu = false
                                showExportSheet = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_add_expense)) },
                            onClick = {
                                showMenu = false
                                onAddExpense()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_add_invoice)) },
                            onClick = {
                                showMenu = false
                                onAddInvoice()
                            }
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        val project = state.project
        if (project == null) {
            Text(
                text = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_not_found),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp)
            )
            return@Scaffold
        }

        if (showExportSheet) {
            ModalBottomSheet(
                onDismissRequest = { showExportSheet = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_export_options_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    ExportToggleRow(
                        title = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_export_include_expenses),
                        checked = includeExpensesInExport,
                        onCheckedChange = { includeExpensesInExport = it }
                    )
                    ExportToggleRow(
                        title = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_export_include_invoices),
                        checked = includeInvoicesInExport,
                        onCheckedChange = { includeInvoicesInExport = it }
                    )
                    Button(
                        onClick = {
                            val report = projectExportService.generateProjectReport(
                                project = project,
                                expenses = state.expenses,
                                invoices = state.invoices,
                                includeExpenses = includeExpensesInExport,
                                includeInvoices = includeInvoicesInExport,
                                currencyCode = currency.code
                            )
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, report)
                            }
                            context.startActivity(
                                Intent.createChooser(
                                    shareIntent,
                                    context.getString(com.yetzira.ContractorCashFlowAndroid.R.string.projects_export_share_chooser)
                                )
                            )
                            showExportSheet = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_export_share_button))
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                FinancialSummaryCard(
                    balance = state.balance,
                    income = state.totalIncome,
                    expenses = state.totalExpenses,
                    currency = currency,
                    profitMargin = state.profitMargin
                )
            }

            item {
                ProjectInfoSection(
                    projectName = project.name,
                    clientName = project.clientName,
                    budget = project.budget,
                    currency = currency,
                    isActive = project.isActive,
                    createdDate = project.createdDate,
                    onClientClick = { onOpenClient(project.clientName) }
                )
            }

            item {
                BudgetUsageBar(
                    utilization = state.budgetUtilization,
                    budget = project.budget,
                    totalExpenses = state.totalExpenses,
                    currency = currency
                )
            }

            item {
                CategoryBreakdownSection(categories = state.categories)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_expenses_section),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = onAddExpense) {
                        Text(text = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_add_expense))
                    }
                }
            }

            items(state.expenses, key = { it.id }) { expense ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value != SwipeToDismissBoxValue.Settled) {
                            viewModel.deleteExpense(expense)
                        }
                        true
                    }
                )
                LaunchedEffect(dismissState.currentValue) {
                    if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                        val result = snackbarHostState.showSnackbar(
                            message = expenseDeletedMessage,
                            actionLabel = undoLabel
                        )
                        if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                            viewModel.undoDeleteExpense()
                        }
                    }
                }
                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.errorContainer)
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            val deleteText = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.common_delete)
                            Text(text = deleteText)
                        }
                    },
                    content = {
                        ExpenseRow(expense = expense, currency = currency, onClick = { onOpenExpense(expense.id) })
                    }
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_invoices_section),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = onAddInvoice) {
                        Text(text = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_add_invoice))
                    }
                }
            }

            items(state.invoices, key = { it.id }) { invoice ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value != SwipeToDismissBoxValue.Settled) {
                            viewModel.deleteInvoice(invoice)
                        }
                        true
                    }
                )
                LaunchedEffect(dismissState.currentValue) {
                    if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                        val result = snackbarHostState.showSnackbar(
                            message = invoiceDeletedMessage,
                            actionLabel = undoLabel
                        )
                        if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                            viewModel.undoDeleteInvoice()
                        }
                    }
                }
                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.errorContainer)
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            val deleteText = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.common_delete)
                            Text(text = deleteText)
                        }
                    },
                    content = {
                        InvoiceRow(invoice = invoice, currency = currency, onClick = { onOpenInvoice(invoice.id) })
                    }
                )
            }
        }
    }
}

@Composable
private fun ExportToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun FinancialSummaryCard(
    balance: Double,
    income: Double,
    expenses: Double,
    currency: CurrencyOption,
    profitMargin: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_net_balance),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatCurrencyAmount(balance, currency),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (balance >= 0) Color(0xFF34C759) else Color(0xFFFF3B30)
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_income),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrencyAmount(income, currency),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF34C759)
                    )
                }
                Column {
                    Text(
                        text = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_expenses),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrencyAmount(expenses, currency),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFF3B30)
                    )
                }
                Column {
                    Text(
                        text = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_profit_margin),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.1f", profitMargin)}%",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectInfoSection(
    projectName: String,
    clientName: String,
    budget: Double,
    currency: CurrencyOption,
    isActive: Boolean,
    createdDate: Long,
    onClientClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    Column(modifier = modifier) {
        Text(
            text = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_info_section).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, bottom = 6.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                ProjectInfoRow(
                    label = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_name),
                    value = projectName
                )
                HorizontalDivider(modifier = Modifier.padding(start = 16.dp), color = dividerColor)
                ProjectInfoRow(
                    label = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_client_name),
                    value = clientName,
                    valueColor = MaterialTheme.colorScheme.primary,
                    onClick = onClientClick
                )
                HorizontalDivider(modifier = Modifier.padding(start = 16.dp), color = dividerColor)
                ProjectInfoRow(
                    label = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_budget),
                    value = formatCurrencyAmount(budget, currency)
                )
                HorizontalDivider(modifier = Modifier.padding(start = 16.dp), color = dividerColor)
                ProjectStatusRow(isActive = isActive)
                HorizontalDivider(modifier = Modifier.padding(start = 16.dp), color = dividerColor)
                ProjectInfoRow(
                    label = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_created_date),
                    value = formatLongDate(createdDate)
                )
            }
        }
    }
}

@Composable
private fun BudgetUsageBar(
    utilization: Double,
    budget: Double,
    totalExpenses: Double,
    currency: CurrencyOption,
    modifier: Modifier = Modifier
) {
    val remaining = (budget - totalExpenses).coerceAtLeast(0.0)
    val barColor = when {
        utilization < 50  -> Color(0xFF34C759)
        utilization <= 80 -> Color(0xFFFF9500)
        else              -> Color(0xFFFF3B30)
    }
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    Column(modifier = modifier) {
        Text(
            text = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_budget_utilization).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, bottom = 6.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Spent row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_budget_spent),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formatCurrencyAmount(totalExpenses, currency),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(start = 16.dp), color = dividerColor)
                // Progress row
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_budget_of_budget, utilization.toFloat()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LinearProgressIndicator(
                        progress = { (utilization / 100.0).coerceIn(0.0, 1.0).toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = barColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(start = 16.dp), color = dividerColor)
                // Remaining row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_budget_remaining),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formatCurrencyAmount(remaining, currency),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF34C759)
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryBreakdownSection(
    categories: List<CategoryBreakdownUi>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_category_breakdown),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                categories.forEach { category ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = category.category,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.1f", category.percent)}%",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        LinearProgressIndicator(
                            progress = { (category.percent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseRow(expense: ExpenseEntity, currency: CurrencyOption, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = expense.descriptionText,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatCurrencyAmount(expense.amount, currency),
                    color = Color(0xFFFF3B30),
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .width(16.dp)
                            .height(16.dp)
                    )
                    Text(
                        text = formatDate(expense.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun InvoiceRow(invoice: InvoiceEntity, currency: CurrencyOption, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .width(18.dp)
                            .height(18.dp)
                    )
                    Text(
                        text = invoice.clientName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = formatCurrencyAmount(invoice.amount, currency),
                    color = if (invoice.isPaid) Color(0xFF34C759) else Color(0xFFFF9500),
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .width(16.dp)
                            .height(16.dp)
                    )
                    Text(
                        text = formatDate(invoice.dueDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    InvoiceStatusBadge(invoice = invoice)
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(active: Boolean) {
    val color = if (active) Color(0xFF34C759) else Color(0xFF8E8E93)
    val label = if (active) stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_active) else stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_inactive)
    Text(
        text = label,
        color = color,
        style = com.yetzira.ContractorCashFlowAndroid.ui.theme.BadgeTextStyle,
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), CircleShape)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Composable
private fun InvoiceStatusBadge(invoice: InvoiceEntity) {
    val now = System.currentTimeMillis()
    val (label, color) = when {
        invoice.isPaid -> stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_invoice_paid) to Color(0xFF34C759)
        invoice.dueDate < now -> stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_invoice_overdue) to Color(0xFFFF3B30)
        else -> stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_invoice_pending) to Color(0xFFFF9500)
    }
    Text(
        text = label,
        color = color,
        style = com.yetzira.ContractorCashFlowAndroid.ui.theme.BadgeTextStyle,
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), CircleShape)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Composable
private fun ProjectInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = valueColor
        )
    }
}

@Composable
private fun ProjectStatusRow(isActive: Boolean, modifier: Modifier = Modifier) {
    val statusColor = if (isActive) Color(0xFF34C759) else Color(0xFF8E8E93)
    val statusLabel = stringResource(
        if (isActive) com.yetzira.ContractorCashFlowAndroid.R.string.projects_status_active
        else com.yetzira.ContractorCashFlowAndroid.R.string.projects_status_inactive
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_status),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(statusColor, CircleShape)
            )
            Text(
                text = statusLabel,
                style = MaterialTheme.typography.bodyLarge,
                color = statusColor
            )
        }
    }
}

private fun formatLongDate(timestamp: Long): String =
    SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(timestamp))

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))


