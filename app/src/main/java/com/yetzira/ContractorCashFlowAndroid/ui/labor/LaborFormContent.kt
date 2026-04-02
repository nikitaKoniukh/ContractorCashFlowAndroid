package com.yetzira.ContractorCashFlowAndroid.ui.labor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yetzira.ContractorCashFlowAndroid.R
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.LaborType
import com.yetzira.ContractorCashFlowAndroid.ui.components.ModernTextField
import com.yetzira.ContractorCashFlowAndroid.ui.components.ModernDropdown

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
        ModernTextField(
            value = state.workerName,
            onValueChange = { onChange(state.copy(workerName = it)) },
            label = stringResource(R.string.labor_form_worker_name_label),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (state.duplicateWarning) {
            Text(text = stringResource(R.string.labor_form_duplicate_warning))
        }

        LaborTypeDropdown(
            selected = state.laborType,
            onSelected = { onChange(state.copy(laborType = it)) },
            modifier = Modifier.fillMaxWidth()
        )

        if (state.laborType == LaborType.HOURLY || state.laborType == LaborType.DAILY) {
            ModernTextField(
                value = state.hourlyRate,
                onValueChange = { onChange(state.copy(hourlyRate = it)) },
                label = stringResource(R.string.labor_form_rate_per_hour_label),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
            ModernTextField(
                value = state.dailyRate,
                onValueChange = { onChange(state.copy(dailyRate = it)) },
                label = stringResource(R.string.labor_form_rate_per_day_label),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        }

        if (state.laborType == LaborType.SUBCONTRACTOR) {
            ModernTextField(
                value = state.contractPrice,
                onValueChange = { onChange(state.copy(contractPrice = it)) },
                label = stringResource(R.string.labor_form_contract_price_label),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        }

        ModernTextField(
            value = state.notes,
            onValueChange = { onChange(state.copy(notes = it)) },
            label = stringResource(R.string.labor_form_notes_label),
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 6
        )
    }
}

@Composable
private fun LaborTypeDropdown(
    selected: LaborType,
    onSelected: (LaborType) -> Unit,
    modifier: Modifier = Modifier
) {
    ModernDropdown(
        label = stringResource(R.string.labor_form_type_label),
        options = LaborType.entries.map { it.name },
        selected = selected.name,
        onSelected = { selectedName ->
            val type = LaborType.entries.find { it.name == selectedName }
            if (type != null) {
                onSelected(type)
            }
        },
        modifier = modifier
    )
}

