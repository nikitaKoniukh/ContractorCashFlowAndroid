package com.yetzira.ContractorCashFlowAndroid.ui.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yetzira.ContractorCashFlowAndroid.R
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesListScreen(
    viewModel: ExpenseViewModel,
    onCreateExpense: () -> Unit,
    onEditExpense: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.listUiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showFilters by remember { mutableStateOf(false) }

    val deletedMessage = "Expense deleted"
    val undoLabel = stringResource(R.string.common_undo)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateExpense) {
                Icon(
                    painter = painterResource(android.R.drawable.ic_input_add),
                    contentDescription = "New Expense"
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = state.query,
                    onValueChange = viewModel::setSearchQuery,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search by description") },
                    singleLine = true
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
                    Text(text = "Filter")
                }
            }

            if (state.expenses.isEmpty()) {
                EmptyExpensesState(onCreateExpense = onCreateExpense)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.expenses, key = { it.expense.id }) { item ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value != SwipeToDismissBoxValue.Settled) {
                                    viewModel.deleteExpense(item.expense)
                                }
                                true
                            }
                        )

                        LaunchedEffect(dismissState.currentValue) {
                            if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                                val result = snackbarHostState.showSnackbar(
                                    message = deletedMessage,
                                    actionLabel = undoLabel
                                )
                                if (result == SnackbarResult.ActionPerformed) {
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
                                        .clip(MaterialTheme.shapes.medium)
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
                painter = painterResource(android.R.drawable.ic_input_add),
                contentDescription = "New Expense"
            )
        }
    }
}

@Composable
private fun ExpenseRow(item: ExpenseListItemUi, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = item.expense.descriptionText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(text = formatMoney(item.expense.amount), color = Color(0xFFFF3B30), fontWeight = FontWeight.SemiBold)
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Badge { Text(item.expense.category) }
                Text(text = item.projectName ?: "No project")
                Text(text = formatDate(item.expense.date))
            }
        }
    }
}

private fun formatMoney(amount: Double): String = String.format(Locale.US, "%.2f", amount)

private fun formatDate(timestamp: Long): String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))

