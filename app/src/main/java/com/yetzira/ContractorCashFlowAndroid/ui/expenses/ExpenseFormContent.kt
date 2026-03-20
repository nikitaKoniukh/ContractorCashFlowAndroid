package com.yetzira.ContractorCashFlowAndroid.ui.expenses

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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yetzira.ContractorCashFlowAndroid.R
import com.yetzira.ContractorCashFlowAndroid.data.preferences.CurrencyOption
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseCategory
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.LaborType
import com.yetzira.ContractorCashFlowAndroid.ui.components.formatAmountInput
import com.yetzira.ContractorCashFlowAndroid.ui.components.ModernTextField
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ExpenseFormContent(
    state: ExpenseFormUiState,
    currency: CurrencyOption,
    onStateChange: (ExpenseFormUiState) -> Unit,
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
                    Text(
                        text = stringResource(R.string.expenses_form_category_label),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    CategoryPicker(
                        selected = state.category,
                        onSelected = { onStateChange(state.copy(category = it)) }
                    )

                    if (state.category == ExpenseCategory.LABOR && state.workers.isNotEmpty()) {
                        WorkerPicker(
                            workers = state.workers,
                            selectedWorkerId = state.workerId,
                            onSelected = { workerId ->
                                onStateChange(
                                    state.copy(workerId = workerId).let {
                                        it
                                    }
                                )
                            }
                        )

                        val worker = state.workers.firstOrNull { it.worker.id == state.workerId }
                        if (worker != null) {
                            Text(
                                text = "${worker.worker.workerName} • ${worker.rate ?: 0.0}${worker.rateSuffix}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (worker.laborType == LaborType.HOURLY || worker.laborType == LaborType.DAILY) {
                                ModernTextField(
                                    value = state.unitsWorked,
                                    onValueChange = { onStateChange(state.copy(unitsWorked = it)) },
                                    label = stringResource(R.string.expenses_form_units_label),
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true
                                )
                            }
                        }
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
                        value = state.description,
                        onValueChange = { onStateChange(state.copy(description = it)) },
                        label = stringResource(R.string.expenses_form_description_label),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    ModernTextField(
                        value = state.amount,
                        onValueChange = {
                            if (!state.isAmountReadOnly) {
                                onStateChange(state.copy(amount = formatAmountInput(it)))
                            }
                        },
                        label = stringResource(R.string.expenses_form_amount_label),
                        modifier = Modifier.fillMaxWidth(),
                        prefix = { Text(currency.symbol) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        readOnly = state.isAmountReadOnly
                    )

                    DatePickerField(
                        date = state.date,
                        onDateSelected = { onStateChange(state.copy(date = it)) }
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
                        text = stringResource(R.string.expenses_form_project_label),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ProjectPicker(
                        projectOptions = state.projects,
                        selectedProjectId = state.projectId,
                        onSelected = { onStateChange(state.copy(projectId = it)) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryPicker(
    selected: ExpenseCategory,
    onSelected: (ExpenseCategory) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        TextField(
            value = selected.name,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            label = { Text(stringResource(R.string.expenses_form_category_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ExpenseCategory.values().forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = {
                        onSelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkerPicker(
    workers: List<WorkerOptionUi>,
    selectedWorkerId: String?,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = workers.firstOrNull { it.worker.id == selectedWorkerId }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        TextField(
            value = selected?.worker?.workerName.orEmpty(),
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            label = { Text(stringResource(R.string.expenses_form_worker_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            workers.forEach { worker ->
                val detail = "${worker.worker.workerName} • ${worker.rate ?: 0.0}${worker.rateSuffix}"
                DropdownMenuItem(
                    text = { Text(detail) },
                    onClick = {
                        onSelected(worker.worker.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun DatePickerField(date: Long, onDateSelected: (Long) -> Unit) {
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
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
            Text(
                text = stringResource(R.string.expenses_form_date_label),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        TextButton(onClick = {
            val cal = Calendar.getInstance().apply { timeInMillis = date }
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
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(date)),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectPicker(
    projectOptions: List<com.yetzira.ContractorCashFlowAndroid.data.local.entity.ProjectEntity>,
    selectedProjectId: String?,
    onSelected: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = projectOptions.firstOrNull { it.id == selectedProjectId }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        TextField(
            value = selected?.name ?: stringResource(R.string.expenses_form_no_project),
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            label = { Text(stringResource(R.string.expenses_form_project_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.expenses_form_no_project)) },
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

