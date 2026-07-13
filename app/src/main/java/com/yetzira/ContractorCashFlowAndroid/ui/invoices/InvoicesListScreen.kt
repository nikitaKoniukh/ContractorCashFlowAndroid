package com.yetzira.ContractorCashFlowAndroid.ui.invoices

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Work
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yetzira.ContractorCashFlowAndroid.R
import com.yetzira.ContractorCashFlowAndroid.data.preferences.CurrencyOption
import com.yetzira.ContractorCashFlowAndroid.data.preferences.UserPreferencesRepository
import com.yetzira.ContractorCashFlowAndroid.ui.components.ModernSearchBar
import com.yetzira.ContractorCashFlowAndroid.ui.components.formatCurrencyAmount
import com.yetzira.ContractorCashFlowAndroid.ui.components.groupedRowShape
import com.yetzira.ContractorCashFlowAndroid.ui.navigation.KablanProLayoutDefaults
import com.yetzira.ContractorCashFlowAndroid.ui.theme.BadgeTextStyle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoicesListScreen(
    viewModel: InvoiceViewModel,
    onCreate: () -> Unit,
    onEdit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val preferencesRepository = remember(context) { UserPreferencesRepository(context.applicationContext) }
    val currency by preferencesRepository.selectedCurrencyCode.collectAsState(initial = CurrencyOption.ILS)
    val state by viewModel.listUiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showFilterMenu by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<InvoiceListItemUi?>(null) }
    val hasActiveFilterOrSearch = state.query.isNotBlank() || state.statusFilter != InvoiceStatusFilter.ALL

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreate) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.invoices_new)
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
                    .padding(top = KablanProLayoutDefaults.TopSectionSpacing),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModernSearchBar(
                    value = state.query,
                    onValueChange = viewModel::setSearchQuery,
                    modifier = Modifier.weight(1f),
                    placeholder = stringResource(R.string.invoices_list_search)
                )
                Box {
                    TextButton(
                        onClick = { showFilterMenu = true },
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.medium)
                            .background(
                                if (state.statusFilter != InvoiceStatusFilter.ALL) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                    ) {
                        Text(text = invoiceFilterLabel(state.statusFilter))
                    }
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        InvoiceStatusFilter.entries.forEach { filter ->
                            DropdownMenuItem(
                                text = { Text(invoiceFilterLabel(filter)) },
                                onClick = {
                                    viewModel.setStatusFilter(filter)
                                    showFilterMenu = false
                                }
                            )
                        }
                    }
                }
            }

            when {
                state.invoices.isEmpty() && hasActiveFilterOrSearch -> {
                    EmptyFilteredInvoicesState()
                }
                state.invoices.isEmpty() -> {
                    EmptyInvoicesState(onCreate = onCreate)
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                        contentPadding = PaddingValues(top = 14.dp, bottom = 92.dp)
                    ) {
                        itemsIndexed(state.invoices, key = { _, item -> item.invoice.id }) { index, item ->
                            val shape = groupedRowShape(index, state.invoices.lastIndex)
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
                                            .clip(shape)
                                            .background(MaterialTheme.colorScheme.errorContainer)
                                            .padding(horizontal = 16.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Text(text = stringResource(R.string.common_delete))
                                    }
                                },
                                content = {
                                    InvoiceRow(
                                        item = item,
                                        currency = currency,
                                        index = index,
                                        lastIndex = state.invoices.lastIndex,
                                        onClick = { onEdit(item.invoice.id) }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    pendingDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(text = stringResource(R.string.common_delete)) },
            text = { Text(text = item.invoice.clientName) },
            confirmButton = {
                TextButton(onClick = {
                    pendingDelete = null
                    viewModel.deleteInvoice(item.invoice)
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
private fun invoiceFilterLabel(filter: InvoiceStatusFilter): String = when (filter) {
    InvoiceStatusFilter.ALL -> stringResource(R.string.invoices_filter_all)
    InvoiceStatusFilter.PAID -> stringResource(R.string.invoices_filter_paid)
    InvoiceStatusFilter.UNPAID -> stringResource(R.string.invoices_filter_unpaid)
    InvoiceStatusFilter.OVERDUE -> stringResource(R.string.invoices_filter_overdue)
}

@Composable
private fun EmptyInvoicesState(onCreate: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.invoices_empty_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.invoices_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )
        FloatingActionButton(onClick = onCreate) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.invoices_new)
            )
        }
    }
}

@Composable
private fun EmptyFilteredInvoicesState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.invoices_empty_filtered_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.invoices_empty_filtered_body),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun InvoiceRow(
    item: InvoiceListItemUi,
    currency: CurrencyOption,
    index: Int,
    lastIndex: Int,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = groupedRowShape(index, lastIndex),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.invoice.clientName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                val amountColor = if (item.invoice.isPaid) {
                    Color(0xFF34C759)
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
                Text(
                    text = formatCurrencyAmount(item.invoice.amount, currency),
                    color = amountColor,
                    fontWeight = FontWeight.SemiBold
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InvoiceStatusBadge(isPaid = item.invoice.isPaid, isOverdue = item.isOverdue)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Work,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.projectName ?: stringResource(R.string.invoices_form_no_project),
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
                        text = formatDate(item.invoice.dueDate),
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
private fun InvoiceStatusBadge(isPaid: Boolean, isOverdue: Boolean) {
    val (label, color) = when {
        isPaid -> stringResource(R.string.projects_invoice_paid) to Color(0xFF34C759)
        isOverdue -> stringResource(R.string.projects_invoice_overdue) to Color(0xFFFF3B30)
        else -> stringResource(R.string.projects_invoice_pending) to Color(0xFFFF9500)
    }
    Text(
        text = label,
        color = color,
        style = BadgeTextStyle,
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), CircleShape)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
