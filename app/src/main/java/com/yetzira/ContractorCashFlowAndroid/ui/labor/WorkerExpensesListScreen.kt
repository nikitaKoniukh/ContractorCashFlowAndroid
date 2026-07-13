package com.yetzira.ContractorCashFlowAndroid.ui.labor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.yetzira.ContractorCashFlowAndroid.data.local.AppDatabase
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseCategory
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseEntity
import com.yetzira.ContractorCashFlowAndroid.data.preferences.CurrencyOption
import com.yetzira.ContractorCashFlowAndroid.data.preferences.UserPreferencesRepository
import com.yetzira.ContractorCashFlowAndroid.ui.components.ModernSearchBar
import com.yetzira.ContractorCashFlowAndroid.ui.components.formatCurrencyAmount
import com.yetzira.ContractorCashFlowAndroid.ui.navigation.KablanProLayoutDefaults
import com.yetzira.ContractorCashFlowAndroid.ui.navigation.KablanProTopBar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerExpensesListScreen(
    workerId: String,
    workerName: String,
    onBack: () -> Unit,
    onOpenExpense: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val preferencesRepository = remember(context) { UserPreferencesRepository(context.applicationContext) }
    val currency by preferencesRepository.selectedCurrencyCode.collectAsState(initial = CurrencyOption.ILS)
    val expenses by remember(workerId) {
        AppDatabase.getInstance(context).expenseDao().getForWorker(workerId)
    }.collectAsState(initial = emptyList())

    var query by remember { mutableStateOf("") }
    val filtered = remember(expenses, query) {
        if (query.isBlank()) expenses
        else expenses.filter { it.descriptionText.contains(query, ignoreCase = true) }
    }
    val grouped = remember(filtered) { groupExpensesByDay(filtered) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier.fillMaxSize(),
        topBar = {
            KablanProTopBar(
                title = stringResource(R.string.labor_worker_expenses_title, workerName),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            ModernSearchBar(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = KablanProLayoutDefaults.TopSectionSpacing),
                placeholder = stringResource(R.string.expenses_search_placeholder)
            )

            if (grouped.isEmpty()) {
                Text(
                    text = stringResource(R.string.expenses_empty_title),
                    modifier = Modifier.padding(24.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 14.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    grouped.forEach { (day, dayExpenses) ->
                        item(key = "header_$day") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dayHeader(day),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = formatCurrencyAmount(dayExpenses.sumOf { it.amount }, currency),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        items(dayExpenses, key = { it.id }) { expense ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenExpense(expense.id) }
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = expense.descriptionText,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = ExpenseCategory.fromString(expense.category)
                                            ?.let { stringResource(it.labelResId) }
                                            ?: expense.category,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = formatCurrencyAmount(expense.amount, currency),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun groupExpensesByDay(expenses: List<ExpenseEntity>): List<Pair<Long, List<ExpenseEntity>>> {
    val cal = Calendar.getInstance()
    return expenses
        .groupBy { expense ->
            cal.timeInMillis = expense.date
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }
        .map { (day, list) -> day to list.sortedByDescending { it.date } }
        .sortedByDescending { it.first }
}

@Composable
private fun dayHeader(dayMillis: Long): String {
    val cal = Calendar.getInstance()
    val todayStart = cal.apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val yesterdayStart = todayStart - 86_400_000L
    return when (dayMillis) {
        todayStart -> stringResource(R.string.common_today)
        yesterdayStart -> stringResource(R.string.common_yesterday)
        else -> SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault()).format(Date(dayMillis))
    }
}
