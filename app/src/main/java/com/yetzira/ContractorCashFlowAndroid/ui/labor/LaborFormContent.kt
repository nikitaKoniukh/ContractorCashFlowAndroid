package com.yetzira.ContractorCashFlowAndroid.ui.labor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yetzira.ContractorCashFlowAndroid.R
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.LaborType
import java.util.Locale

private val WarningOrange = Color(0xFFFF9500)

@Composable
fun LaborFormContent(
    state: LaborFormUiState,
    onChange: (LaborFormUiState) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        FormSection(title = stringResource(R.string.labor_form_section_basic)) {
            LaborIosTextField(
                value = state.workerName,
                onValueChange = { onChange(state.copy(workerName = it)) },
                placeholder = stringResource(R.string.labor_form_worker_name_label),
                singleLine = true
            )
            HorizontalDivider(
                modifier = Modifier.padding(start = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            LaborTypeSegmentedControl(
                selected = state.laborType,
                onSelected = { onChange(state.copy(laborType = it)) },
                modifier = Modifier.padding(16.dp)
            )
        }

        if (state.duplicateWarning) {
            Row(
                modifier = Modifier.padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = WarningOrange
                )
                Text(
                    text = stringResource(R.string.labor_form_duplicate_warning),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        FormSection(title = stringResource(R.string.labor_form_section_rates)) {
            when (state.laborType) {
                LaborType.HOURLY, LaborType.DAILY -> {
                    LaborIosTextField(
                        value = state.hourlyRate,
                        onValueChange = { onChange(state.copy(hourlyRate = it)) },
                        placeholder = stringResource(R.string.labor_form_rate_per_hour_label),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    LaborIosTextField(
                        value = state.dailyRate,
                        onValueChange = { onChange(state.copy(dailyRate = it)) },
                        placeholder = stringResource(R.string.labor_form_rate_per_day_label),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
                LaborType.SUBCONTRACTOR -> {
                    LaborIosTextField(
                        value = state.contractPrice,
                        onValueChange = { onChange(state.copy(contractPrice = it)) },
                        placeholder = stringResource(R.string.labor_form_contract_price_label),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            }
        }

        FormSection(title = stringResource(R.string.labor_form_section_notes)) {
            LaborIosTextField(
                value = state.notes,
                onValueChange = { onChange(state.copy(notes = it)) },
                placeholder = stringResource(R.string.labor_form_notes_label),
                singleLine = false,
                minLines = 3,
                maxLines = 6
            )
        }
    }
}

@Composable
private fun FormSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title.uppercase(Locale.getDefault()),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp)
        )
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
private fun LaborTypeSegmentedControl(
    selected: LaborType,
    onSelected: (LaborType) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            LaborType.entries.forEach { type ->
                SegmentedControlItem(
                    text = stringResource(type.labelResId),
                    selected = selected == type,
                    onClick = { onSelected(type) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SegmentedControlItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun LaborIosTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        keyboardOptions = keyboardOptions,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        )
    )
}
