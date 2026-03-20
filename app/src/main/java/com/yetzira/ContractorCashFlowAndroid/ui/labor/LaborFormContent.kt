package com.yetzira.ContractorCashFlowAndroid.ui.labor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yetzira.ContractorCashFlowAndroid.R
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.LaborType

@Composable
fun LaborFormContent(
    state: LaborFormUiState,
    onChange: (LaborFormUiState) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TextField(
            value = state.workerName,
            onValueChange = { onChange(state.copy(workerName = it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.labor_form_worker_name_label)) },
            singleLine = true
        )

        if (state.duplicateWarning) {
            Text(text = stringResource(R.string.labor_form_duplicate_warning))
        }

        LaborTypePicker(
            selected = state.laborType,
            onSelected = { onChange(state.copy(laborType = it)) }
        )

        if (state.laborType == LaborType.HOURLY || state.laborType == LaborType.DAILY) {
            TextField(
                value = state.hourlyRate,
                onValueChange = { onChange(state.copy(hourlyRate = it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.labor_form_rate_per_hour_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
            TextField(
                value = state.dailyRate,
                onValueChange = { onChange(state.copy(dailyRate = it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.labor_form_rate_per_day_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        }

        if (state.laborType == LaborType.CONTRACT || state.laborType == LaborType.SUBCONTRACTOR) {
            TextField(
                value = state.contractPrice,
                onValueChange = { onChange(state.copy(contractPrice = it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.labor_form_contract_price_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        }

        TextField(
            value = state.notes,
            onValueChange = { onChange(state.copy(notes = it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.labor_form_notes_label)) },
            minLines = 3,
            maxLines = 6
        )
    }
}

@Composable
private fun LaborTypePicker(
    selected: LaborType,
    onSelected: (LaborType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(text = stringResource(R.string.labor_form_type_label))
        TextButton(onClick = { expanded = true }) {
            Text(selected.name)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            LaborType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.name) },
                    onClick = {
                        onSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

