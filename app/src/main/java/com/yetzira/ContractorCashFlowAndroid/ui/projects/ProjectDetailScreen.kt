package com.yetzira.ContractorCashFlowAndroid.ui.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.InvoiceEntity
import com.yetzira.ContractorCashFlowAndroid.data.preferences.CurrencyOption
import com.yetzira.ContractorCashFlowAndroid.data.preferences.UserPreferencesRepository
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val preferencesRepository = remember(context) { UserPreferencesRepository(context.applicationContext) }
    val currency by preferencesRepository.selectedCurrencyCode.collectAsState(initial = CurrencyOption.ILS)
    val state by viewModel.detailUiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showMenu by remember { mutableStateOf(false) }
    val expenseDeletedMessage = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_expense_deleted)
    val invoiceDeletedMessage = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_invoice_deleted)
    val undoLabel = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.common_undo)

    LaunchedEffect(projectId) {
        viewModel.selectProject(projectId)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = state.project?.name ?: stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_detail_title)) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text(stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.common_back)) }
                },
                actions = {
                    TextButton(onClick = { showMenu = true }) {
                        Text(stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.common_more))
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
                            onClick = { showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_add_expense)) },
                            onClick = { showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_add_invoice)) },
                            onClick = { showMenu = false }
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

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                BudgetUsageBar(utilization = state.budgetUtilization)
            }

            item {
                CategoryBreakdownSection(categories = state.categories)
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_expenses_section),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { showMenu = true }) {
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
                    backgroundContent = {},
                    content = {
                        ExpenseRow(expense = expense, currency = currency, onClick = { showMenu = true })
                        
                    }
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_invoices_section),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { showMenu = true }) {
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
                    backgroundContent = {},
                    content = {
                        InvoiceRow(invoice = invoice, currency = currency, onClick = { showMenu = true })
                    }
                )
            }
        }
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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
            .padding(16.dp)
    ) {
        Text(text = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_net_balance), style = MaterialTheme.typography.labelLarge)
        Text(
            text = formatCurrencyAmount(balance, currency),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = if (balance >= 0) Color(0xFF34C759) else Color(0xFFFF3B30)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "${stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_income)}: ${formatCurrencyAmount(income, currency)}", color = Color(0xFF34C759))
            Text(text = "${stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_expenses)}: ${formatCurrencyAmount(expenses, currency)}", color = Color(0xFFFF3B30))
        }
        Text(
            text = "${stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_profit_margin)}: ${String.format(Locale.US, "%.1f", profitMargin)}%",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
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
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = projectName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 6.dp)) {
            TextButton(onClick = onClientClick) {
                Text(text = clientName)
            }
            StatusBadge(active = isActive)
        }
        Text(text = "${stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_budget)}: ${formatCurrencyAmount(budget, currency)}")
        Text(text = "${stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_created_date)}: ${formatDate(createdDate)}")
    }
}

@Composable
private fun BudgetUsageBar(utilization: Double, modifier: Modifier = Modifier) {
    val color = when {
        utilization < 50 -> Color(0xFF34C759)
        utilization <= 80 -> Color(0xFFFF9500)
        else -> Color(0xFFFF3B30)
    }
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = "${stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_budget_usage)}: ${String.format(Locale.US, "%.1f", utilization)}%")
        LinearProgressIndicator(
            progress = { (utilization / 100.0).coerceIn(0.0, 1.0).toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp),
            color = color
        )
    }
}

@Composable
private fun CategoryBreakdownSection(
    categories: List<CategoryBreakdownUi>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = stringResource(com.yetzira.ContractorCashFlowAndroid.R.string.projects_category_breakdown), style = MaterialTheme.typography.titleMedium)
        categories.forEach { category ->
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = category.category)
                    Text(text = "${String.format(Locale.US, "%.1f", category.percent)}%")
                }
                LinearProgressIndicator(
                    progress = { (category.percent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ExpenseRow(expense: ExpenseEntity, currency: CurrencyOption, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = expense.descriptionText, fontWeight = FontWeight.SemiBold)
                Text(text = formatCurrencyAmount(expense.amount, currency), color = Color(0xFFFF3B30), fontWeight = FontWeight.SemiBold)
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row {
                    Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.padding(top = 2.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = formatDate(expense.date), style = MaterialTheme.typography.bodySmall)
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
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(top = 2.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = invoice.clientName, fontWeight = FontWeight.SemiBold)
                }
                Text(
                    text = formatCurrencyAmount(invoice.amount, currency),
                    color = if (invoice.isPaid) Color(0xFF34C759) else Color(0xFFFF9500),
                    fontWeight = FontWeight.SemiBold
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = formatDate(invoice.dueDate), style = MaterialTheme.typography.bodySmall)
                Row {
                    InvoiceStatusBadge(invoice = invoice)
                    Spacer(modifier = Modifier.width(6.dp))
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
        color = Color.White,
        modifier = Modifier
            .background(color, MaterialTheme.shapes.small)
            .padding(horizontal = 8.dp, vertical = 2.dp)
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
        color = Color.White,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier
            .background(color, MaterialTheme.shapes.small)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))


