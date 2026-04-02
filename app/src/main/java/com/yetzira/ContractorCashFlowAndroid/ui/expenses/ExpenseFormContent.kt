package com.yetzira.ContractorCashFlowAndroid.ui.expenses

import android.app.DatePickerDialog
import android.net.Uri as AndroidUri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.yetzira.ContractorCashFlowAndroid.R
import com.yetzira.ContractorCashFlowAndroid.data.preferences.CurrencyOption
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseCategory
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.LaborType
import com.yetzira.ContractorCashFlowAndroid.ui.components.formatAmountInput
import com.yetzira.ContractorCashFlowAndroid.ui.components.parseAmountInput
import com.yetzira.ContractorCashFlowAndroid.ui.components.toFormattedCurrency
import com.yetzira.ContractorCashFlowAndroid.ui.components.ModernTextField
import com.yetzira.ContractorCashFlowAndroid.ui.components.ModernDropdown
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ExpenseFormContent(
    state: ExpenseFormUiState,
    currency: CurrencyOption,
    onStateChange: (ExpenseFormUiState) -> Unit,
    onUnitsWorkedChanged: (String) -> Unit = { },
    onDateAdded: (Long) -> Unit = { },
    onDateRemoved: (Long) -> Unit = { },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
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
                    ModernDropdown(
                        label = stringResource(R.string.expenses_form_category_label),
                        options = ExpenseCategory.entries.map { it.name },
                        selected = state.category.name,
                        onSelected = { selectedName ->
                            val selected = ExpenseCategory.entries.find { it.name == selectedName }
                            if (selected != null) {
                                onStateChange(state.copy(
                                    category = selected,
                                    workerId = null,
                                    unitsWorked = "",
                                    laborTypeSnapshot = null,
                                    notes = "",
                                    selectedDates = emptyList()
                                ))
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (state.category == ExpenseCategory.LABOR && state.workers.isNotEmpty()) {
                        ModernDropdown(
                            label = stringResource(R.string.expenses_form_worker_label),
                            options = state.workers.map { it.worker.workerName },
                            selected = state.workers.firstOrNull { it.worker.id == state.workerId }?.worker?.workerName.orEmpty(),
                            onSelected = { workerName ->
                                val worker = state.workers.find { it.worker.workerName == workerName }
                                if (worker != null) {
                                    onStateChange(state.copy(
                                        workerId = worker.worker.id,
                                        laborTypeSnapshot = null,
                                        unitsWorked = "",
                                        selectedDates = emptyList()
                                    ))
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        val worker = state.workers.firstOrNull { it.worker.id == state.workerId }
                        if (worker != null) {
                            val hasHourly = worker.hourlyRate != null
                            val hasDaily = worker.dailyRate != null
                            val hasSubcontractor = worker.contractPrice != null
                            val selectedLaborMode = state.laborTypeSnapshot

                            // Build list of available labor types for this worker
                            val availableLaborTypes = mutableListOf<LaborType>()
                            if (hasHourly) availableLaborTypes.add(LaborType.HOURLY)
                            if (hasDaily) availableLaborTypes.add(LaborType.DAILY)
                            if (hasSubcontractor) availableLaborTypes.add(LaborType.SUBCONTRACTOR)

                            // Always show labor type selector if multiple options available or to allow override
                            if (availableLaborTypes.size > 1 || availableLaborTypes.size == 1) {
                                val hourlyLabel = stringResource(R.string.expenses_form_labor_mode_hourly)
                                val dailyLabel = stringResource(R.string.expenses_form_labor_mode_daily)
                                val subcontractorLabel = stringResource(R.string.labor_type_subcontractor)

                                val typeOptions = availableLaborTypes.map { type ->
                                    when (type) {
                                        LaborType.HOURLY -> hourlyLabel
                                        LaborType.DAILY -> dailyLabel
                                        LaborType.SUBCONTRACTOR -> subcontractorLabel
                                    }
                                }

                                val labelToTypeMap = mapOf(
                                    hourlyLabel to LaborType.HOURLY,
                                    dailyLabel to LaborType.DAILY,
                                    subcontractorLabel to LaborType.SUBCONTRACTOR
                                )

                                val selectedModeLabel = when (selectedLaborMode) {
                                    LaborType.HOURLY -> hourlyLabel
                                    LaborType.DAILY -> dailyLabel
                                    LaborType.SUBCONTRACTOR -> subcontractorLabel
                                    else -> if (availableLaborTypes.isNotEmpty()) {
                                        when (availableLaborTypes.first()) {
                                            LaborType.HOURLY -> hourlyLabel
                                            LaborType.DAILY -> dailyLabel
                                            LaborType.SUBCONTRACTOR -> subcontractorLabel
                                        }
                                    } else ""
                                }

                                ModernDropdown(
                                    label = stringResource(R.string.expenses_form_labor_mode_label),
                                    options = typeOptions,
                                    selected = selectedModeLabel,
                                    onSelected = { modeLabel ->
                                        val selectedMode = labelToTypeMap[modeLabel] ?: availableLaborTypes.firstOrNull()
                                        onStateChange(state.copy(
                                            laborTypeSnapshot = selectedMode,
                                            unitsWorked = "",
                                            selectedDates = emptyList()
                                        ))
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            val effectiveLaborType = selectedLaborMode ?: availableLaborTypes.firstOrNull()
                            val effectiveRate = when (effectiveLaborType) {
                                LaborType.HOURLY -> worker.hourlyRate
                                LaborType.DAILY -> worker.dailyRate
                                LaborType.SUBCONTRACTOR -> worker.contractPrice
                                null -> null
                            }

                            Text(
                                text = "${worker.worker.workerName} • ${effectiveRate ?: 0.0}${effectiveLaborType?.rateSuffix.orEmpty()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (effectiveLaborType == LaborType.HOURLY) {
                                ModernTextField(
                                    value = state.unitsWorked,
                                    onValueChange = onUnitsWorkedChanged,
                                    label = stringResource(R.string.expenses_form_hours_worked_label),
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true
                                )
                            }
                        }
                    }

                    // For labor: show notes field; for others: show description
                    if (state.category == ExpenseCategory.LABOR) {
                        ModernTextField(
                            value = state.notes,
                            onValueChange = { onStateChange(state.copy(notes = it)) },
                            label = stringResource(R.string.expenses_form_notes_label),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = false,
                            minLines = 2
                        )
                    } else {
                        ModernTextField(
                            value = state.description,
                            onValueChange = { onStateChange(state.copy(description = it)) },
                            label = stringResource(R.string.expenses_form_description_label),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    if (state.useMultiDatePicker) {
                        // Multi-date picker UI
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.expenses_form_amount_label),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = (parseAmountInput(state.amount) ?: 0.0).toFormattedCurrency(currency),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (state.amount.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (state.selectedDates.isEmpty()) Icons.Default.CalendarToday else Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = if (state.selectedDates.isEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (state.selectedDates.isEmpty()) {
                                            stringResource(R.string.expenses_form_select_days)
                                        } else {
                                            "${state.selectedDayCount} day${if (state.selectedDayCount == 1) "" else "s"} selected"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (state.selectedDates.isEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // Simple date selection display
                        Text(
                            text = stringResource(R.string.expenses_form_selected_dates),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = {
                                val cal = Calendar.getInstance().apply { timeInMillis = state.date }
                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        val picked = Calendar.getInstance().apply {
                                            set(year, month, day, 0, 0, 0)
                                            set(Calendar.MILLISECOND, 0)
                                        }.timeInMillis
                                        onDateAdded(picked)
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                        ) {
                            Text(stringResource(R.string.expenses_form_add_day))
                        }
                        if (state.selectedDates.isNotEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                state.selectedDates.forEach { dateMillis ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(dateMillis)),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        TextButton(onClick = { onDateRemoved(dateMillis) }) {
                                            Text(stringResource(R.string.action_delete))
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Single date/amount picker mode
                        ModernTextField(
                            value = state.amount,
                            onValueChange = {
                                if (!state.isAmountReadOnly) {
                                    onStateChange(state.copy(amount = formatAmountInput(it)))
                                }
                            },
                            label = stringResource(R.string.expenses_form_amount_label),
                            modifier = Modifier.fillMaxWidth(),
                            suffix = { Text(currency.symbol) },
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
                    ModernDropdown(
                        label = stringResource(R.string.expenses_form_project_label),
                        options = listOf(stringResource(R.string.expenses_form_no_project)) + state.projects.map { it.name },
                        selected = state.projects.firstOrNull { it.id == state.projectId }?.name ?: stringResource(R.string.expenses_form_no_project),
                        onSelected = { projectName ->
                            val selectedProject = state.projects.find { it.name == projectName }
                            onStateChange(state.copy(projectId = selectedProject?.id))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Receipt image thumbnail (if present)
        if (state.receiptImageUri != null) {
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
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = AndroidUri.parse(state.receiptImageUri),
                            contentDescription = stringResource(R.string.scan_receipt_image),
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Text(
                            text = stringResource(R.string.scan_receipt_attached),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
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


