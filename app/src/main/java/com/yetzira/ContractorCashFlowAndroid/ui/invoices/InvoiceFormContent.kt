package com.yetzira.ContractorCashFlowAndroid.ui.invoices

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yetzira.ContractorCashFlowAndroid.R
import com.yetzira.ContractorCashFlowAndroid.data.preferences.CurrencyOption
import com.yetzira.ContractorCashFlowAndroid.ui.components.formatAmountInput
import com.yetzira.ContractorCashFlowAndroid.ui.components.ModernTextField
import com.yetzira.ContractorCashFlowAndroid.ui.components.ModernDropdown
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun InvoiceFormContent(
    state: InvoiceFormUiState,
    currency: CurrencyOption,
    onStateChange: (InvoiceFormUiState) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.invoices_client_mode_existing),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Switch(
                            checked = state.useExistingClient,
                            onCheckedChange = { onStateChange(state.copy(useExistingClient = it)) }
                        )
                    }

                    if (state.useExistingClient) {
                        ModernDropdown(
                            label = stringResource(R.string.invoices_select_existing_client),
                            options = state.existingClients.map { it.name },
                            selected = state.selectedClientName,
                            onSelected = { onStateChange(state.copy(selectedClientName = it)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        ModernTextField(
                            value = state.enteredClientName,
                            onValueChange = { onStateChange(state.copy(enteredClientName = it)) },
                            label = stringResource(R.string.invoices_client_name),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ModernTextField(
                        value = state.amount,
                        onValueChange = { onStateChange(state.copy(amount = formatAmountInput(it))) },
                        label = stringResource(R.string.invoices_amount),
                        modifier = Modifier.fillMaxWidth(),
                        prefix = { Text(currency.symbol) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )

                    DueDatePicker(
                        dueDate = state.dueDate,
                        onDateSelected = { onStateChange(state.copy(dueDate = it)) }
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.invoices_project_optional),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ModernDropdown(
                        label = stringResource(R.string.invoices_project_optional),
                        options = listOf(stringResource(R.string.invoices_no_project)) + state.projects.map { it.name },
                        selected = state.projects.firstOrNull { it.id == state.projectId }?.name ?: stringResource(R.string.invoices_no_project),
                        onSelected = { projectName ->
                            val selectedProject = state.projects.find { it.name == projectName }
                            onStateChange(state.copy(projectId = selectedProject?.id))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.invoices_paid_toggle),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Switch(
                        checked = state.isPaid,
                        onCheckedChange = { onStateChange(state.copy(isPaid = it)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DueDatePicker(dueDate: Long, onDateSelected: (Long) -> Unit) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
            Text(
                text = stringResource(R.string.invoices_due_date),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        TextButton(onClick = {
            val cal = Calendar.getInstance().apply { timeInMillis = dueDate }
            DatePickerDialog(
                context,
                { _, year, month, day ->
                    val picked = Calendar.getInstance().apply {
                        set(year, month, day, 0, 0, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    onDateSelected(picked)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }) {
            Text(
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(dueDate)),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
