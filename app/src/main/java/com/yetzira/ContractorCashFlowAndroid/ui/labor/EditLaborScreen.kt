package com.yetzira.ContractorCashFlowAndroid.ui.labor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.unit.dp
import com.yetzira.ContractorCashFlowAndroid.R
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.LaborType
import com.yetzira.ContractorCashFlowAndroid.data.preferences.CurrencyOption
import com.yetzira.ContractorCashFlowAndroid.data.preferences.UserPreferencesRepository
import com.yetzira.ContractorCashFlowAndroid.ui.components.formatCurrencyAmount
import com.yetzira.ContractorCashFlowAndroid.ui.navigation.KablanProLayoutDefaults
import com.yetzira.ContractorCashFlowAndroid.ui.navigation.KablanProTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLaborScreen(
    workerId: String,
    viewModel: LaborViewModel,
    onBack: () -> Unit,
    onOpenExpenses: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val preferencesRepository = remember(context) { UserPreferencesRepository(context.applicationContext) }
    val currency by preferencesRepository.selectedCurrencyCode.collectAsState(initial = CurrencyOption.ILS)
    val listState by viewModel.listUiState.collectAsState()
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
        formState = viewModel.updateForm(
            viewModel.buildFormState(detailState.worker),
            listState.workers.map { it.worker }
        )
    }

    val worker = detailState.worker
    val metrics = detailState.metrics

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
                            formState = viewModel.updateForm(
                                viewModel.buildFormState(detailState.worker),
                                listState.workers.map { it.worker }
                            )
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
        },
        bottomBar = {
            if (isEditing) {
                TextButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.padding(8.dp)
                ) { Text(stringResource(R.string.action_delete)) }
            }
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
                    onChange = {
                        formState = viewModel.updateForm(it, listState.workers.map { metric -> metric.worker })
                    }
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .padding(top = KablanProLayoutDefaults.TopSectionSpacing)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DetailRow(
                            label = stringResource(R.string.labor_form_worker_name_label),
                            value = worker.workerName
                        )
                        HorizontalDivider()
                        DetailRow(
                            label = stringResource(R.string.labor_form_type_label),
                            value = stringResource(
                                LaborType.fromString(worker.laborType)?.labelResId
                                    ?: R.string.labor_type_hourly
                            )
                        )
                        HorizontalDivider()
                        if (worker.hourlyRate != null) {
                            DetailRow(
                                label = stringResource(R.string.labor_form_rate_per_hour_label),
                                value = formatCurrencyAmount(worker.hourlyRate, currency)
                            )
                            HorizontalDivider()
                        }
                        if (worker.dailyRate != null) {
                            DetailRow(
                                label = stringResource(R.string.labor_form_rate_per_day_label),
                                value = formatCurrencyAmount(worker.dailyRate, currency)
                            )
                            HorizontalDivider()
                        }
                        if (worker.contractPrice != null) {
                            DetailRow(
                                label = stringResource(R.string.labor_form_contract_price_label),
                                value = formatCurrencyAmount(worker.contractPrice, currency)
                            )
                            HorizontalDivider()
                        }
                        if (!worker.notes.isNullOrBlank()) {
                            DetailRow(
                                label = stringResource(R.string.labor_form_notes_label),
                                value = worker.notes
                            )
                        }
                    }
                }

                if (metrics != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.labor_worker_statistics),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            DetailRow(
                                label = stringResource(R.string.labor_total_earned),
                                value = formatCurrencyAmount(metrics.totalAmountEarned, currency)
                            )
                            DetailRow(
                                label = stringResource(R.string.labor_units_worked),
                                value = String.format(java.util.Locale.US, "%.2f", metrics.totalUnitsWorked)
                            )
                            DetailRow(
                                label = stringResource(R.string.labor_days_worked),
                                value = metrics.totalDaysWorked.toString()
                            )
                            if (metrics.associatedProjects.isNotEmpty()) {
                                DetailRow(
                                    label = stringResource(R.string.labor_projects_label),
                                    value = metrics.associatedProjects.joinToString()
                                )
                            }
                        }
                    }
                }

                if ((metrics?.linkedExpenseCount ?: 0) > 0) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenExpenses(workerId) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.labor_view_expenses),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = stringResource(
                                        R.string.labor_linked_expenses_count,
                                        metrics?.linkedExpenseCount ?: 0
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
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
private fun DetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
