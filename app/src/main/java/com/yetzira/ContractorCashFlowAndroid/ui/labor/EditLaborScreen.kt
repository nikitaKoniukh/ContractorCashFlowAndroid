package com.yetzira.ContractorCashFlowAndroid.ui.labor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yetzira.ContractorCashFlowAndroid.R
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseCategory
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.LaborDetailsEntity
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.LaborType
import com.yetzira.ContractorCashFlowAndroid.data.preferences.CurrencyOption
import com.yetzira.ContractorCashFlowAndroid.data.preferences.UserPreferencesRepository
import com.yetzira.ContractorCashFlowAndroid.ui.components.StatPill
import com.yetzira.ContractorCashFlowAndroid.ui.components.WorkerAvatar
import com.yetzira.ContractorCashFlowAndroid.ui.components.formatCurrencyAmount
import com.yetzira.ContractorCashFlowAndroid.ui.expenses.displayExpenseDescription
import com.yetzira.ContractorCashFlowAndroid.ui.expenses.expenseCategoryLabel
import com.yetzira.ContractorCashFlowAndroid.ui.navigation.KablanProLayoutDefaults
import com.yetzira.ContractorCashFlowAndroid.ui.navigation.KablanProTopBar
import com.yetzira.ContractorCashFlowAndroid.ui.theme.BadgeTextStyle
import com.yetzira.ContractorCashFlowAndroid.ui.theme.KablanProColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLaborScreen(
    workerId: String,
    viewModel: LaborViewModel,
    onBack: () -> Unit,
    onOpenExpenses: (String) -> Unit = {},
    onOpenExpense: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val preferencesRepository = remember(context) { UserPreferencesRepository(context.applicationContext) }
    val currency by preferencesRepository.selectedCurrencyCode.collectAsState(initial = CurrencyOption.ILS)
    val detailState by viewModel.detailUiState.collectAsState()
    var formState by remember { mutableStateOf(LaborFormUiState()) }
    var isEditing by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(workerId) {
        viewModel.selectWorker(workerId)
        isEditing = false
    }

    LaunchedEffect(detailState.worker?.id) {
        viewModel.setOriginalWorker(detailState.worker)
        formState = viewModel.updateForm(viewModel.buildFormState(detailState.worker))
    }

    val worker = detailState.worker
    val metrics = detailState.metrics
    val laborType = LaborType.fromString(worker?.laborType)

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier.fillMaxSize(),
        topBar = {
            KablanProTopBar(
                title = if (isEditing) {
                    stringResource(R.string.labor_screen_edit_title)
                } else {
                    worker?.workerName ?: stringResource(R.string.tab_labor)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isEditing) {
                            isEditing = false
                            formState = viewModel.updateForm(viewModel.buildFormState(detailState.worker))
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(
                                if (isEditing) R.string.common_cancel else R.string.common_back
                            )
                        )
                    }
                },
                actions = {
                    if (isEditing) {
                        TextButton(
                            onClick = {
                                viewModel.saveWorker(formState, onDone = { isEditing = false })
                            },
                            enabled = formState.canSave
                        ) { Text(stringResource(R.string.action_save)) }
                    } else {
                        TextButton(onClick = { isEditing = true }) {
                            Text(stringResource(R.string.common_edit))
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (worker == null) {
            Text(
                text = stringResource(R.string.labor_worker_not_found),
                modifier = Modifier.padding(innerPadding).padding(24.dp)
            )
            return@Scaffold
        }

        if (isEditing) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .padding(top = KablanProLayoutDefaults.TopSectionSpacing)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LaborFormContent(
                    state = formState,
                    onChange = { formState = viewModel.updateForm(it) }
                )
                TextButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.action_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .padding(top = KablanProLayoutDefaults.TopSectionSpacing)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                WorkerHeaderCard(
                    name = worker.workerName,
                    typeLabel = stringResource(laborType?.labelResId ?: R.string.labor_type_hourly)
                )

                RatesCard(worker = worker, laborType = laborType, currency = currency)

                if (!worker.notes.isNullOrBlank()) {
                    NotesCard(notes = worker.notes)
                }

                if (metrics != null && metrics.linkedExpenseCount > 0) {
                    StatsCard(metrics = metrics, currency = currency)

                    ExpensesPreviewCard(
                        expenses = detailState.recentExpenses,
                        totalCount = metrics.linkedExpenseCount,
                        currency = currency,
                        onShowAll = { onOpenExpenses(workerId) },
                        onOpenExpense = onOpenExpense
                    )
                }

                CreatedFooter(createdDate = worker.createdDate)

                TextButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        text = stringResource(R.string.action_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        val linkedCount = metrics?.linkedExpenseCount ?: 0
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.labor_delete_worker_title)) },
            text = {
                Text(
                    if (linkedCount > 0) {
                        stringResource(
                            R.string.labor_delete_worker_message_with_linked,
                            worker?.workerName.orEmpty(),
                            linkedCount
                        )
                    } else {
                        stringResource(
                            R.string.labor_delete_worker_message,
                            worker?.workerName.orEmpty()
                        )
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    worker?.let { viewModel.deleteWorker(it, onDone = onBack) }
                }) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}

@Composable
private fun WorkerHeaderCard(name: String, typeLabel: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WorkerAvatar(name = name, size = 64.dp)
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = typeLabel,
                style = BadgeTextStyle,
                color = KablanProColors.WorkerPurple,
                modifier = Modifier
                    .background(
                        KablanProColors.WorkerPurple.copy(alpha = 0.1f),
                        CircleShape
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun RatesCard(
    worker: LaborDetailsEntity,
    laborType: LaborType?,
    currency: CurrencyOption
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.labor_form_section_rates),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(16.dp)
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            when (laborType) {
                LaborType.SUBCONTRACTOR -> {
                    if (worker.contractPrice != null) {
                        DetailRow(
                            label = stringResource(R.string.labor_form_contract_price_label),
                            value = formatCurrencyAmount(worker.contractPrice, currency),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                else -> {
                    var showedHourly = false
                    if (worker.hourlyRate != null) {
                        DetailRow(
                            label = stringResource(R.string.labor_form_rate_per_hour_label),
                            value = formatCurrencyAmount(worker.hourlyRate, currency),
                            modifier = Modifier.padding(16.dp)
                        )
                        showedHourly = true
                    }
                    if (worker.dailyRate != null) {
                        if (showedHourly) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                        }
                        DetailRow(
                            label = stringResource(R.string.labor_form_rate_per_day_label),
                            value = formatCurrencyAmount(worker.dailyRate, currency),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotesCard(notes: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.labor_form_notes_label),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(16.dp)
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Text(
                text = notes,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun StatsCard(metrics: WorkerMetricsUi, currency: CurrencyOption) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.labor_worker_statistics),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(16.dp)
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = formatCurrencyAmount(metrics.totalAmountEarned, currency),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.labor_total_earned),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val showHours = metrics.hourlyUnitsWorked > 0
            val showDays = metrics.dailyUnitsWorked > 0
            if (showHours || showDays) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (showHours) {
                        StatPill(
                            value = String.format(Locale.US, "%.1f", metrics.hourlyUnitsWorked),
                            label = stringResource(R.string.labor_pill_hours),
                            icon = Icons.Default.Schedule,
                            color = KablanProColors.HourlyTeal
                        )
                    }
                    if (showDays) {
                        StatPill(
                            value = String.format(Locale.US, "%.1f", metrics.dailyUnitsWorked),
                            label = stringResource(R.string.labor_pill_days),
                            icon = Icons.Default.CalendarToday,
                            color = KablanProColors.PendingOrange
                        )
                    }
                }
            }

            if (metrics.associatedProjects.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.labor_projects_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    metrics.associatedProjects.forEach { projectName ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = KablanProColors.BudgetBlue,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = projectName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = KablanProColors.BudgetBlue
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpensesPreviewCard(
    expenses: List<ExpenseEntity>,
    totalCount: Int,
    currency: CurrencyOption,
    onShowAll: () -> Unit,
    onOpenExpense: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.tab_expenses),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (totalCount > 3) {
                    Text(
                        text = stringResource(R.string.labor_show_all_expenses, totalCount),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(onClick = onShowAll)
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            expenses.forEachIndexed { index, expense ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                }
                ExpensePreviewRow(
                    expense = expense,
                    currency = currency,
                    onClick = { onOpenExpense(expense.id) }
                )
            }
        }
    }
}

@Composable
private fun ExpensePreviewRow(
    expense: ExpenseEntity,
    currency: CurrencyOption,
    onClick: () -> Unit
) {
    val category = ExpenseCategory.fromString(expense.category)
    val title = displayExpenseDescription(expense.descriptionText).ifBlank {
        expenseCategoryLabel(category).ifBlank { expense.category }
    }
    val dateLabel = remember(expense.date) {
        SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(expense.date))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1
            )
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = formatCurrencyAmount(expense.amount, currency),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
private fun CreatedFooter(createdDate: Long) {
    val formatted = remember(createdDate) {
        SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(createdDate))
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.labor_created_date),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = formatted,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
