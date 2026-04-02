package com.yetzira.ContractorCashFlowAndroid.ui.labor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.IconButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.TopAppBar
import com.yetzira.ContractorCashFlowAndroid.R
import com.yetzira.ContractorCashFlowAndroid.data.preferences.CurrencyOption
import com.yetzira.ContractorCashFlowAndroid.data.preferences.UserPreferencesRepository
import com.yetzira.ContractorCashFlowAndroid.ui.components.StatPill
import com.yetzira.ContractorCashFlowAndroid.ui.components.WorkerAvatar
import com.yetzira.ContractorCashFlowAndroid.ui.components.formatCurrencyAmount
import com.yetzira.ContractorCashFlowAndroid.ui.theme.BadgeTextStyle
import com.yetzira.ContractorCashFlowAndroid.ui.theme.BodyMediumSemibold
import com.yetzira.ContractorCashFlowAndroid.ui.theme.KablanProColors
import com.yetzira.ContractorCashFlowAndroid.ui.theme.SectionHeaderStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaborListScreen(
    viewModel: LaborViewModel,
    onMenuClick: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.listUiState.collectAsState()
    var showFilters by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<WorkerMetricsUi?>(null) }

    val context = LocalContext.current
    val preferencesRepository = remember(context) { UserPreferencesRepository(context.applicationContext) }
    val currency by preferencesRepository.selectedCurrencyCode.collectAsState(initial = CurrencyOption.ILS)

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.tab_labor)) },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = stringResource(id = R.string.menu_open)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter"
                        )
                    }
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "Sort"
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
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
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Worker")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {


            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                item {
                    SummaryCard(summary = state.summary, currency = currency)
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.labor_list).uppercase(),
                            style = SectionHeaderStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${state.workers.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

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
                        modifier = Modifier.padding(vertical = 5.dp),
                        state = dismissState,
                        backgroundContent = {
                            val isSwiping =
                                dismissState.currentValue != SwipeToDismissBoxValue.Settled ||
                                dismissState.targetValue != SwipeToDismissBoxValue.Settled
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSwiping) MaterialTheme.colorScheme.errorContainer else Color.Transparent)
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                if (isSwiping) {
                                    Text(text = "Delete")
                                }
                            }
                        },
                        content = {
                            WorkerCard(
                                worker = worker,
                                currency = currency,
                                onClick = { onEdit(worker.worker.id) },
                                modifier = Modifier
                            )
                        }
                    )
                }

                // Extra bottom space so the final card clears the FAB area.
                item { Spacer(modifier = Modifier.size(96.dp)) }
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

// ── Summary Card ─────────────────────────────────────────────────────────────

@Composable
private fun SummaryCard(summary: LaborSummaryUi, currency: CurrencyOption) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = summary.periodLabel.uppercase(),
            style = SectionHeaderStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 2.dp)
        )

        // Row 1: Worker Cost + Total Workers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AllTimeMetricCard(
                label = stringResource(R.string.labor_summary_total_cost),
                value = formatCurrencyAmount(summary.totalLaborCost, currency),
                icon = Icons.Default.AttachMoney,
                color = KablanProColors.BudgetBlue,
                modifier = Modifier.weight(1f)
            )
            AllTimeMetricCard(
                label = stringResource(R.string.labor_summary_total_workers),
                value = "${summary.workerCount}",
                icon = Icons.Default.Group,
                color = KablanProColors.WorkerPurple,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: keep the bottom row visible so ALL TIME is always a 2x2 grid.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AllTimeMetricCard(
                label = stringResource(R.string.labor_summary_total_days),
                value = "${summary.daysWorked}",
                icon = Icons.Default.CalendarToday,
                color = KablanProColors.PendingOrange,
                modifier = Modifier.weight(1f)
            )
            AllTimeMetricCard(
                label = stringResource(R.string.labor_summary_total_hours),
                value = "${summary.totalHours.toInt()}",
                icon = Icons.Default.Schedule,
                color = KablanProColors.HourlyTeal,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AllTimeMetricCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ── Worker Card ───────────────────────────────────────────────────────────────

@Composable
private fun WorkerCard(
    worker: WorkerMetricsUi,
    currency: CurrencyOption,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // ── Header: avatar + name/rate + type badge ───────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WorkerAvatar(name = worker.worker.workerName)

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = worker.worker.workerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    val rateLabel = worker.rateLabel
                    if (rateLabel.isNotBlank() && rateLabel != "-") {
                        Text(
                            text = rateLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Labor type badge
                Text(
                    text = worker.laborType?.name
                        ?.lowercase()
                        ?.replaceFirstChar { it.uppercase() }
                        ?: "Unknown",
                    style = BadgeTextStyle,
                    color = KablanProColors.WorkerPurple,
                    modifier = Modifier
                        .background(
                            KablanProColors.WorkerPurple.copy(alpha = 0.12f),
                            CircleShape
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // ── Stats pills: hours + days ─────────────────────────────────
            val showHours = worker.hourlyUnitsWorked > 0
            val showDays = worker.dailyUnitsWorked > 0
            if (showHours || showDays) {
                Row(
                    modifier = Modifier.padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (showHours) {
                        StatPill(
                            value = "${worker.hourlyUnitsWorked.toInt()}",
                            label = stringResource(R.string.labor_pill_hours),
                            icon = Icons.Default.Schedule,
                            color = KablanProColors.HourlyTeal
                        )
                    }
                    if (showDays) {
                        StatPill(
                            value = "${worker.dailyUnitsWorked.toInt()}",
                            label = stringResource(R.string.labor_pill_days),
                            icon = Icons.Default.CalendarToday,
                            color = KablanProColors.PendingOrange
                        )
                    }
                }
            }

            // ── Projects breakdown ────────────────────────────────────────
            if (worker.projectBreakdown.isNotEmpty()) {
                Column(
                    modifier = Modifier.padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    worker.projectBreakdown.forEach { project ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = project.projectName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                                maxLines = 1
                            )
                            Text(
                                text = formatCurrencyAmount(project.amount, currency),
                                style = BodyMediumSemibold
                            )
                        }
                    }
                }
            }

            // ── Total Amount footer ───────────────────────────────────────
            HorizontalDivider(
                modifier = Modifier.padding(top = 10.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.labor_total_amount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatCurrencyAmount(worker.totalAmountEarned, currency),
                    style = BodyMediumSemibold,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

