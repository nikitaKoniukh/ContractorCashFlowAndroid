package com.yetzira.ContractorCashFlowAndroid.ui.labor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yetzira.ContractorCashFlowAndroid.R
import com.yetzira.ContractorCashFlowAndroid.ui.components.ModernSearchBar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaborListScreen(
    viewModel: LaborViewModel,
    onAdd: () -> Unit,
    onEdit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.listUiState.collectAsState()
    var showFilters by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<WorkerMetricsUi?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Worker"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            ModernSearchBar(
                value = state.query,
                onValueChange = viewModel::setSearchQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                placeholder = stringResource(R.string.labor_search_placeholder)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = { showFilters = true }) {
                    Text(if (state.filters.isActive) "Filters On" else "Filter")
                }
                Box {
                    TextButton(onClick = { showSortMenu = true }) { Text(state.sort.name) }
                    DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                        LaborSortOption.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.name) },
                                onClick = {
                                    viewModel.setSort(option)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }

            SummaryCard(summary = state.summary)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.workers, key = { it.worker.id }) { worker ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value != SwipeToDismissBoxValue.Settled) {
                                pendingDelete = worker
                                false
                            } else true
                        }
                    )

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
                                Text(text = "Delete")
                            }
                        },
                        content = {
                            WorkerCard(worker = worker, onClick = { onEdit(worker.worker.id) })
                        }
                    )
                }
            }
        }
    }

    if (showFilters) {
        LaborFiltersBottomSheet(
            initial = state.filters,
            availableProjects = state.availableProjects,
            availableMonths = state.availableMonths,
            onDone = {
                viewModel.applyFilters(it)
                showFilters = false
            },
            onClearAll = {
                viewModel.applyFilters(LaborFiltersState())
                showFilters = false
            },
            onDismiss = { showFilters = false }
        )
    }

    pendingDelete?.let { worker ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete worker?") },
            text = {
                Text(
                    if (worker.linkedExpenseCount > 0) {
                        "${worker.worker.workerName} will be deleted. ${worker.linkedExpenseCount} linked expense(s) will remain but unlinked."
                    } else {
                        "${worker.worker.workerName} will be deleted."
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteWorker(worker.worker)
                    pendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SummaryCard(summary: LaborSummaryUi) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = summary.periodLabel, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "Total Labor Cost: ${formatMoney(summary.totalLaborCost)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Worker Count: ${summary.workerCount}")
                Text(text = "Days Worked: ${summary.daysWorked}")
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Avg Daily Cost: ${formatMoney(summary.avgDailyCost)}")
                Text(text = "Total Hours: ${String.format(Locale.US, "%.2f", summary.totalHours)}")
            }
        }
    }
}

@Composable
private fun WorkerCard(worker: WorkerMetricsUi, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = worker.worker.workerName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = worker.laborType?.name ?: "Unknown",
                    color = com.yetzira.ContractorCashFlowAndroid.ui.theme.KablanProColors.WorkerPurple,
                    style = com.yetzira.ContractorCashFlowAndroid.ui.theme.BadgeTextStyle,
                    modifier = Modifier
                        .background(
                            com.yetzira.ContractorCashFlowAndroid.ui.theme.KablanProColors.WorkerPurple.copy(alpha = 0.15f),
                            androidx.compose.foundation.shape.CircleShape
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Earned: ${formatMoney(worker.totalAmountEarned)}", fontWeight = FontWeight.SemiBold)
                Icon(
                    imageVector = Icons.Default.Work,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 2.dp)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Rate: ${worker.rateLabel}")
                Text(text = "Days: ${worker.totalDaysWorked}")
                Text(text = "Units: ${String.format(Locale.US, "%.2f", worker.totalUnitsWorked)}")
            }

            if (worker.associatedProjects.isNotEmpty()) {
                Text(text = "Projects: ${worker.associatedProjects.joinToString()} ")
            }
            if (!worker.worker.notes.isNullOrBlank()) {
                Text(text = worker.worker.notes, maxLines = 2)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

private fun formatMoney(amount: Double): String = String.format(Locale.US, "%.2f", amount)

