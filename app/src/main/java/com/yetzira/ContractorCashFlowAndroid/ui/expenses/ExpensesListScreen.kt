package com.yetzira.ContractorCashFlowAndroid.ui.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DocumentScanner
import com.yetzira.ContractorCashFlowAndroid.R
import com.yetzira.ContractorCashFlowAndroid.data.preferences.CurrencyOption
import com.yetzira.ContractorCashFlowAndroid.data.preferences.UserPreferencesRepository
import com.yetzira.ContractorCashFlowAndroid.ui.components.ModernSearchBar
import com.yetzira.ContractorCashFlowAndroid.ui.components.formatCurrencyAmount
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesListScreen(
    viewModel: ExpenseViewModel,
    onCreateExpense: () -> Unit,
    onEditExpense: (String) -> Unit,
    onScanReceipt: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.listUiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showFilters by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<ExpenseListItemUi?>(null) }

    Scaffold(
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = onScanReceipt,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(
                        Icons.Default.DocumentScanner,
                        contentDescription = stringResource(R.string.scan_title)
                    )
                }
                FloatingActionButton(onClick = onCreateExpense) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "New Expense"
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModernSearchBar(
                    value = state.query,
                    onValueChange = viewModel::setSearchQuery,
                    modifier = Modifier.weight(1f),
                    placeholder = stringResource(R.string.expenses_list_search)
                )
                TextButton(
                    onClick = { showFilters = true },
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.medium)
                        .background(
                            if (state.filters.isActive) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                ) {
                    Text(text = stringResource(R.string.expenses_list_filter_button))
                }
            }

            if (state.expenses.isEmpty()) {
                EmptyExpensesState(onCreateExpense = onCreateExpense)
            } else {
                val sections = remember(state.expenses) { groupExpensesByDate(state.expenses) }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 14.dp, bottom = 92.dp)
                ) {
                    sections.forEach { section ->
                        item(key = "header_${section.dayStartMillis}") {
                            ExpenseDateSectionHeader(labelRes = section.labelRes, dateFallback = section.dateFallback)
                        }

                        items(section.items, key = { it.expense.id }) { item ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { value ->
                                    if (value != SwipeToDismissBoxValue.Settled) {
                                        pendingDelete = item
                                        return@rememberSwipeToDismissBoxState false
                                    }
                                    true
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
                                        Text(text = stringResource(R.string.common_delete))
                                    }
                                },
                                content = {
                                    ExpenseRow(item = item, onClick = { onEditExpense(item.expense.id) })
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showFilters) {
        ExpenseFiltersBottomSheet(
            initial = state.filters,
            onApply = {
                viewModel.applyFilters(it)
                showFilters = false
            },
            onClearAll = {
                viewModel.clearFilters()
                showFilters = false
            },
            onDismiss = { showFilters = false }
        )
    }

    pendingDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(text = stringResource(R.string.common_delete)) },
            text = { Text(text = item.expense.descriptionText) },
            confirmButton = {
                TextButton(onClick = {
                    pendingDelete = null
                    viewModel.deleteExpense(item.expense)
                }) {
                    Text(text = stringResource(R.string.common_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(text = stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun ExpenseDateSectionHeader(labelRes: Int?, dateFallback: String) {
    val label = labelRes?.let { stringResource(it) } ?: dateFallback
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
}

@Composable
private fun EmptyExpensesState(onCreateExpense: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No expenses yet",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Add your first expense to start tracking costs.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )
        FloatingActionButton(onClick = onCreateExpense) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "New Expense"
            )
        }
    }
}

@Composable
private fun ExpenseRow(item: ExpenseListItemUi, onClick: () -> Unit) {
    val context = LocalContext.current
    val preferencesRepository = remember(context) { UserPreferencesRepository(context.applicationContext) }
    val currency by preferencesRepository.selectedCurrencyCode.collectAsState(initial = CurrencyOption.ILS)
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.expense.descriptionText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(text = formatCurrencyAmount(item.expense.amount, currency), color = Color(0xFFFF3B30), fontWeight = FontWeight.SemiBold)
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val categoryColor = com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseCategory
                    .fromString(item.expense.category)?.let {
                        Color(it.chartColor)
                    } ?: MaterialTheme.colorScheme.primary
                
                Surface(
                    color = categoryColor.copy(alpha = 0.15f),
                    shape = androidx.compose.foundation.shape.CircleShape
                ) {
                    Text(
                        text = item.expense.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = com.yetzira.ContractorCashFlowAndroid.ui.theme.BadgeTextStyle,
                        color = categoryColor
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Work,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.projectName ?: stringResource(R.string.expenses_form_no_project),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatDate(item.expense.date),
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


private fun formatDate(timestamp: Long): String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))

private data class ExpenseDateSection(
    val dayStartMillis: Long,
    val labelRes: Int?,
    val dateFallback: String,
    val items: List<ExpenseListItemUi>
)

private fun groupExpensesByDate(expenses: List<ExpenseListItemUi>): List<ExpenseDateSection> {
    if (expenses.isEmpty()) return emptyList()

    val grouped = linkedMapOf<Long, MutableList<ExpenseListItemUi>>()
    expenses.forEach { item ->
        val dayStart = normalizeDay(item.expense.date)
        grouped.getOrPut(dayStart) { mutableListOf() }.add(item)
    }

    return grouped.map { (dayStart, items) ->
        val todayStart = normalizeDay(System.currentTimeMillis())
        val yesterdayStart = normalizeDay(System.currentTimeMillis() - 86_400_000L)
        val labelRes = when (dayStart) {
            todayStart -> R.string.common_today
            yesterdayStart -> R.string.common_yesterday
            else -> null
        }
        ExpenseDateSection(
            dayStartMillis = dayStart,
            labelRes = labelRes,
            dateFallback = formatDate(dayStart),
            items = items
        )
    }
}

private fun normalizeDay(timestamp: Long): Long {
    val cal = Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}

