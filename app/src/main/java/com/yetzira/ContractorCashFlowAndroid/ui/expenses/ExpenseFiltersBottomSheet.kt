package com.yetzira.ContractorCashFlowAndroid.ui.expenses

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yetzira.ContractorCashFlowAndroid.R
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseCategory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseFiltersBottomSheet(
    initial: ExpenseFilterState,
    onApply: (ExpenseFilterState) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    var state by remember { mutableStateOf(initial) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = stringResource(R.string.expenses_filters_title), style = MaterialTheme.typography.titleMedium)

            Text(text = stringResource(R.string.expenses_filters_category_label))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.category == null,
                    onClick = { state = state.copy(category = null) },
                    label = { Text(stringResource(R.string.expenses_filters_all)) }
                )
                ExpenseCategory.values().forEach { category ->
                    FilterChip(
                        selected = state.category == category,
                        onClick = { state = state.copy(category = category) },
                        label = { Text(category.name) }
                    )
                }
            }

            DateFilterRow(
                title = stringResource(R.string.expenses_filter_start_date),
                enabled = state.hasStartDate,
                currentDate = state.startDate,
                onEnabledChange = { enabled ->
                    state = state.copy(
                        hasStartDate = enabled,
                        startDate = if (enabled) state.startDate ?: System.currentTimeMillis() else null
                    )
                },
                onDateChange = { state = state.copy(startDate = it, hasStartDate = true) }
            )

            DateFilterRow(
                title = stringResource(R.string.expenses_filter_end_date),
                enabled = state.hasEndDate,
                currentDate = state.endDate,
                onEnabledChange = { enabled ->
                    state = state.copy(
                        hasEndDate = enabled,
                        endDate = if (enabled) state.endDate ?: System.currentTimeMillis() else null
                    )
                },
                onDateChange = { state = state.copy(endDate = it, hasEndDate = true) }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onClearAll) { Text(text = stringResource(R.string.expenses_filters_clear)) }
                TextButton(onClick = { onApply(state) }) {
                    Text(text = stringResource(R.string.expenses_filters_apply))
                }
            }
        }
    }
}

@Composable
private fun DateFilterRow(
    title: String,
    enabled: Boolean,
    currentDate: Long?,
    onEnabledChange: (Boolean) -> Unit,
    onDateChange: (Long) -> Unit
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = title)
            TextButton(onClick = { if (enabled) openDatePicker(context, currentDate, onDateChange) }) {
                Text(
                    text = if (enabled && currentDate != null) formatDate(currentDate) else "Pick date"
                )
            }
        }
        Switch(checked = enabled, onCheckedChange = onEnabledChange)
    }
}

private fun openDatePicker(
    context: android.content.Context,
    current: Long?,
    onPicked: (Long) -> Unit
) {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = current ?: System.currentTimeMillis()
    }
    DatePickerDialog(
        context,
        { _, year, month, day ->
            val picked = Calendar.getInstance().apply {
                set(year, month, day, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            onPicked(picked)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))

