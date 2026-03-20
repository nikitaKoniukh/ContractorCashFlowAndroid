package com.yetzira.ContractorCashFlowAndroid.ui.invoices

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yetzira.ContractorCashFlowAndroid.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun InvoiceFormContent(
    state: InvoiceFormUiState,
    onStateChange: (InvoiceFormUiState) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = stringResource(R.string.invoices_client_mode_existing))
            Switch(
                checked = state.useExistingClient,
                onCheckedChange = { onStateChange(state.copy(useExistingClient = it)) }
            )
        }

        if (state.useExistingClient) {
            ExistingClientPicker(
                options = state.existingClients.map { it.name },
                selected = state.selectedClientName,
                onSelected = { onStateChange(state.copy(selectedClientName = it)) }
            )
        } else {
            TextField(
                value = state.enteredClientName,
                onValueChange = { onStateChange(state.copy(enteredClientName = it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.invoices_client_name)) },
                singleLine = true
            )
        }

        TextField(
            value = state.amount,
            onValueChange = { onStateChange(state.copy(amount = it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.invoices_amount)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        DueDatePicker(
            dueDate = state.dueDate,
            onDateSelected = { onStateChange(state.copy(dueDate = it)) }
        )

        ProjectPicker(
            selectedProjectId = state.projectId,
            projectOptions = state.projects,
            onSelected = { onStateChange(state.copy(projectId = it)) }
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = stringResource(R.string.invoices_paid_toggle))
            Switch(
                checked = state.isPaid,
                onCheckedChange = { onStateChange(state.copy(isPaid = it)) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExistingClientPicker(options: List<String>, selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        TextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            label = { Text(stringResource(R.string.invoices_select_existing_client)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { name ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onSelected(name)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun DueDatePicker(dueDate: Long, onDateSelected: (Long) -> Unit) {
    val context = LocalContext.current
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = stringResource(R.string.invoices_due_date))
        androidx.compose.material3.TextButton(onClick = {
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
            Text(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(dueDate)))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectPicker(
    selectedProjectId: String?,
    projectOptions: List<com.yetzira.ContractorCashFlowAndroid.data.local.entity.ProjectEntity>,
    onSelected: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = projectOptions.firstOrNull { it.id == selectedProjectId }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        TextField(
            value = selected?.name ?: stringResource(R.string.invoices_no_project),
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            label = { Text(stringResource(R.string.invoices_project_optional)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.invoices_no_project)) },
                onClick = {
                    onSelected(null)
                    expanded = false
                }
            )
            projectOptions.forEach { project ->
                DropdownMenuItem(
                    text = { Text(project.name) },
                    onClick = {
                        onSelected(project.id)
                        expanded = false
                    }
                )
            }
        }
    }
}


