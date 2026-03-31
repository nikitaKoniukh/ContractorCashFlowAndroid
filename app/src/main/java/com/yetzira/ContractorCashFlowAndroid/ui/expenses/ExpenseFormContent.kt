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
                    ModernDropdown(
                        label = stringResource(R.string.expenses_form_category_label),
                        options = ExpenseCategory.entries.map { it.name },
                        selected = state.category.name,
                        onSelected = { selectedName ->
                            val selected = ExpenseCategory.entries.find { it.name == selectedName }
                            if (selected != null) {
                                onStateChange(state.copy(category = selected))
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
                                    onStateChange(state.copy(workerId = worker.worker.id, laborTypeSnapshot = null))
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        val worker = state.workers.firstOrNull { it.worker.id == state.workerId }
                        if (worker != null) {
                            val hasHourly = worker.hourlyRate != null
                            val hasDaily = worker.dailyRate != null
                            val selectedLaborMode = state.laborTypeSnapshot

                            if (hasHourly && hasDaily) {
                                val hourlyLabel = stringResource(R.string.expenses_form_labor_mode_hourly)
                                val dailyLabel = stringResource(R.string.expenses_form_labor_mode_daily)
                                val selectedModeLabel = when (selectedLaborMode) {
                                    LaborType.HOURLY -> hourlyLabel
                                    LaborType.DAILY -> dailyLabel
                                    else -> ""
                                }

                                ModernDropdown(
                                    label = stringResource(R.string.expenses_form_labor_mode_label),
                                    options = listOf(hourlyLabel, dailyLabel),
                                    selected = selectedModeLabel,
                                    onSelected = { modeLabel ->
                                        val selectedMode = when (modeLabel) {
                                            hourlyLabel -> LaborType.HOURLY
                                            dailyLabel -> LaborType.DAILY
                                            else -> null
                                        }
                                        onStateChange(state.copy(laborTypeSnapshot = selectedMode))
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            val effectiveLaborType = when {
                                hasHourly && hasDaily -> selectedLaborMode
                                hasHourly -> LaborType.HOURLY
                                hasDaily -> LaborType.DAILY
                                else -> worker.laborType
                            }
                            val effectiveRate = when (effectiveLaborType) {
                                LaborType.HOURLY -> worker.hourlyRate
                                LaborType.DAILY -> worker.dailyRate
                                LaborType.CONTRACT, LaborType.SUBCONTRACTOR -> worker.contractPrice
                                null -> null
                            }

                            Text(
                                text = "${worker.worker.workerName} • ${effectiveRate ?: 0.0}${effectiveLaborType?.rateSuffix.orEmpty()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (effectiveLaborType == LaborType.HOURLY || effectiveLaborType == LaborType.DAILY) {
                                ModernTextField(
                                    value = state.unitsWorked,
                                    onValueChange = { onStateChange(state.copy(unitsWorked = it)) },
                                    label = if (effectiveLaborType == LaborType.DAILY) {
                                        stringResource(R.string.expenses_form_days_worked_label)
                                    } else {
                                        stringResource(R.string.expenses_form_hours_worked_label)
                                    },
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

        // Notes field
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
                    ModernTextField(
                        value = state.notes,
                        onValueChange = { onStateChange(state.copy(notes = it)) },
                        label = stringResource(R.string.expenses_form_notes_label),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        minLines = 3
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
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.foundation.layout.Row(
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


