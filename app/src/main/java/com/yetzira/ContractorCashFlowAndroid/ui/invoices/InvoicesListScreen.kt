package com.yetzira.ContractorCashFlowAndroid.ui.invoices

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yetzira.ContractorCashFlowAndroid.R
import com.yetzira.ContractorCashFlowAndroid.billing.PurchaseManagerProvider
import com.yetzira.ContractorCashFlowAndroid.billing.PurchaseViewModel
import com.yetzira.ContractorCashFlowAndroid.billing.PurchaseViewModelFactory
import com.yetzira.ContractorCashFlowAndroid.data.preferences.CurrencyOption
import com.yetzira.ContractorCashFlowAndroid.data.preferences.UserPreferencesRepository
import com.yetzira.ContractorCashFlowAndroid.ui.components.ModernSearchBar
import com.yetzira.ContractorCashFlowAndroid.ui.components.formatCurrencyAmount
import com.yetzira.ContractorCashFlowAndroid.ui.paywall.PaywallScreen
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
    val purchaseManager = remember(context) { PurchaseManagerProvider.getInstance(context.applicationContext) }
    val purchaseViewModel: PurchaseViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = remember { PurchaseViewModelFactory(context) }
    )
    val currency by preferencesRepository.selectedCurrencyCode.collectAsState(initial = CurrencyOption.ILS)
    val state by viewModel.listUiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showFilterMenu by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<InvoiceListItemUi?>(null) }
    var showPaywall by remember { mutableStateOf(false) }
    var paywallMessage by remember { mutableStateOf<String?>(null) }

    val onCreateAttempt = {
        if (purchaseManager.canCreateInvoice(state.invoices.size)) {
            onCreate()
        } else {
            paywallMessage = context.getString(R.string.paywall_limit_invoices)
            showPaywall = true
        }
    }

    Scaffold(
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateAttempt) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Invoice"
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
                ModernSearchBar(
                    value = state.query,
                    onValueChange = viewModel::setSearchQuery,
                    modifier = Modifier.weight(1f),
                    placeholder = stringResource(R.string.invoices_list_search)
                )
                Box {
                    TextButton(onClick = { showFilterMenu = true }) {
                        Text(text = state.statusFilter.name)
                    }
                    androidx.compose.material3.DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        InvoiceStatusFilter.entries.forEach { filter ->
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text(filter.name) },
                                onClick = {
                                    viewModel.setStatusFilter(filter)
                                    showFilterMenu = false
                                }
                            )
                        }
                    }
                }
            }

            if (state.invoices.isEmpty()) {
                EmptyInvoicesState(onCreate = onCreateAttempt)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 14.dp, bottom = 92.dp)
                ) {
                    items(state.invoices, key = { it.invoice.id }) { item ->
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
                                InvoiceRow(item = item, currency = currency, onClick = { onEdit(item.invoice.id) })
                            }
                        )
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

    if (showPaywall) {
        ModalBottomSheet(onDismissRequest = { showPaywall = false }) {
            PaywallScreen(
                viewModel = purchaseViewModel,
                onDismiss = { showPaywall = false },
                limitReachedMessage = paywallMessage
            )
        }
    }
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
            text = "No invoices yet",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Create your first invoice to start tracking income.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )
        FloatingActionButton(onClick = onCreate) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "New Invoice"
            )
        }
    }
}

@Composable
private fun InvoiceRow(item: InvoiceListItemUi, currency: CurrencyOption, onClick: () -> Unit) {
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
                    text = item.invoice.clientName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                val amountColor = if (item.invoice.isPaid) Color(0xFF34C759) else MaterialTheme.colorScheme.onSurface
                Text(text = formatCurrencyAmount(item.invoice.amount, currency), color = amountColor)
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
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
        style = com.yetzira.ContractorCashFlowAndroid.ui.theme.BadgeTextStyle,
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), androidx.compose.foundation.shape.CircleShape)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))


