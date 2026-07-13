package com.yetzira.ContractorCashFlowAndroid.ui.expenses

import android.app.DatePickerDialog
import android.net.Uri as AndroidUri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.yetzira.ContractorCashFlowAndroid.R
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ExpenseCategory
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.LaborType
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ProjectEntity
import com.yetzira.ContractorCashFlowAndroid.data.preferences.CurrencyOption
import com.yetzira.ContractorCashFlowAndroid.ui.components.formatAmountInput
import com.yetzira.ContractorCashFlowAndroid.ui.components.parseAmountInput
import com.yetzira.ContractorCashFlowAndroid.ui.components.toFormattedCurrency
import com.yetzira.ContractorCashFlowAndroid.ui.theme.KablanProColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

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
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            ExpenseFormSection(title = stringResource(R.string.expenses_section_details)) {
                ExpenseDropdownRow(
                    label = stringResource(R.string.expenses_form_category_label),
                    options = ExpenseCategory.entries.map { it to expenseCategoryLabel(it) },
                    selectedLabel = expenseCategoryLabel(state.category),
                    onSelected = { selected ->
                        onStateChange(
                            state.copy(
                                category = selected,
                                workerId = null,
                                unitsWorked = "",
                                laborTypeSnapshot = null,
                                notes = "",
                                selectedDates = emptyList(),
                                calculatedAmount = null
                            )
                        )
                    }
                )

                if (state.category == ExpenseCategory.LABOR && state.workers.isNotEmpty()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    ExpenseStringDropdownRow(
                        placeholder = stringResource(R.string.expenses_form_worker_label),
                        options = state.workers.map { it.worker.workerName },
                        selected = state.workers.firstOrNull { it.worker.id == state.workerId }
                            ?.worker?.workerName.orEmpty(),
                        onSelected = { workerName ->
                            val worker = state.workers.find { it.worker.workerName == workerName }
                            if (worker != null) {
                                onStateChange(
                                    state.copy(
                                        workerId = worker.worker.id,
                                        laborTypeSnapshot = null,
                                        unitsWorked = "",
                                        selectedDates = emptyList(),
                                        calculatedAmount = null
                                    )
                                )
                            }
                        }
                    )

                    val worker = state.workers.firstOrNull { it.worker.id == state.workerId }
                    if (worker != null) {
                        val availableLaborTypes = buildList {
                            if (worker.hourlyRate != null) add(LaborType.HOURLY)
                            if (worker.dailyRate != null) add(LaborType.DAILY)
                            if (worker.contractPrice != null) add(LaborType.SUBCONTRACTOR)
                        }
                        val selectedLaborMode = state.laborTypeSnapshot
                        val effectiveLaborType = selectedLaborMode ?: availableLaborTypes.firstOrNull()

                        if (availableLaborTypes.isNotEmpty()) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                            val typeOptions = availableLaborTypes.map { type ->
                                type to laborTypeLabel(type)
                            }
                            ExpenseDropdownRow(
                                label = stringResource(R.string.expenses_form_labor_mode_label),
                                options = typeOptions,
                                selectedLabel = laborTypeLabel(
                                    effectiveLaborType ?: availableLaborTypes.first()
                                ),
                                onSelected = { selectedMode ->
                                    onStateChange(
                                        state.copy(
                                            laborTypeSnapshot = selectedMode,
                                            unitsWorked = "",
                                            selectedDates = emptyList(),
                                            calculatedAmount = null
                                        )
                                    )
                                }
                            )
                        }

                        val effectiveRate = when (effectiveLaborType) {
                            LaborType.HOURLY -> worker.hourlyRate
                            LaborType.DAILY -> worker.dailyRate
                            LaborType.SUBCONTRACTOR -> worker.contractPrice
                            null -> null
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "${worker.worker.workerName} • ${effectiveRate ?: 0.0}${effectiveLaborType?.rateSuffix.orEmpty()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )

                        if (effectiveLaborType == LaborType.HOURLY) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                            ExpenseIosTextField(
                                value = state.unitsWorked,
                                onValueChange = onUnitsWorkedChanged,
                                placeholder = stringResource(R.string.expenses_form_hours_worked_label),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(start = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                if (state.category == ExpenseCategory.LABOR) {
                    ExpenseIosTextField(
                        value = state.notes,
                        onValueChange = { onStateChange(state.copy(notes = it)) },
                        placeholder = stringResource(R.string.expenses_form_notes_label),
                        singleLine = false,
                        minLines = 2
                    )
                } else {
                    ExpenseIosTextField(
                        value = state.description,
                        onValueChange = { onStateChange(state.copy(description = it)) },
                        placeholder = stringResource(R.string.expenses_form_description_label),
                        singleLine = true
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(start = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                if (state.useMultiDatePicker) {
                    ExpenseIosTextField(
                        value = state.amount,
                        onValueChange = {
                            if (!state.isAmountReadOnly) {
                                onStateChange(state.copy(amount = formatAmountInput(it)))
                            }
                        },
                        placeholder = stringResource(R.string.expenses_form_amount_label),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        prefix = currency.symbol,
                        readOnly = state.isAmountReadOnly
                    )
                    SuggestedAmountHint(state = state, currency = currency)
                    MultiDayStatusRow(selectedDayCount = state.selectedDayCount)
                    EmbeddedMultiDatePicker(
                        selectedDates = state.selectedDates,
                        initialMonthMillis = state.selectedDates.firstOrNull() ?: state.date,
                        onToggleDate = { dateMillis, isSelected ->
                            if (isSelected) {
                                onDateRemoved(dateMillis)
                            } else {
                                onDateAdded(dateMillis)
                            }
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                    )
                } else {
                    ExpenseIosTextField(
                        value = state.amount,
                        onValueChange = {
                            if (!state.isAmountReadOnly) {
                                onStateChange(state.copy(amount = formatAmountInput(it)))
                            }
                        },
                        placeholder = stringResource(R.string.expenses_form_amount_label),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        prefix = currency.symbol,
                        readOnly = state.isAmountReadOnly
                    )
                    SuggestedAmountHint(state = state, currency = currency)
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    DatePickerField(
                        date = state.date,
                        onDateSelected = { onStateChange(state.copy(date = it)) }
                    )
                }
            }
        }

        item {
            ExpenseFormSection(title = stringResource(R.string.expenses_form_project_label)) {
                ExpenseProjectDropdown(
                    projects = state.projects,
                    projectId = state.projectId,
                    onSelected = { onStateChange(state.copy(projectId = it)) }
                )
            }
        }

        if (state.receiptImageUri != null) {
            item {
                ExpenseFormSection(title = stringResource(R.string.scan_receipt_attached)) {
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
                            text = stringResource(R.string.scan_receipt_hint),
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
private fun SuggestedAmountHint(
    state: ExpenseFormUiState,
    currency: CurrencyOption
) {
    if (state.category != ExpenseCategory.LABOR || state.isAmountReadOnly) return
    val calculated = state.calculatedAmount ?: return
    val entered = parseAmountInput(state.amount) ?: 0.0
    if (abs(calculated - entered) < 0.005) return

    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = KablanProColors.BudgetBlue,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = stringResource(
                R.string.expenses_form_suggested_amount,
                calculated.toFormattedCurrency(currency)
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MultiDayStatusRow(selectedDayCount: Int) {
    val isEmpty = selectedDayCount == 0
    val statusColor = if (isEmpty) KablanProColors.PendingOrange else KablanProColors.IncomeGreen
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isEmpty) Icons.Default.Info else Icons.Default.CheckCircle,
            contentDescription = null,
            tint = statusColor,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = if (isEmpty) {
                stringResource(R.string.expenses_form_select_days)
            } else if (selectedDayCount == 1) {
                stringResource(R.string.expenses_form_day_selected)
            } else {
                stringResource(R.string.expenses_form_days_selected, selectedDayCount)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = statusColor
        )
    }
}

@Composable
private fun ExpenseFormSection(
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
private fun <T> ExpenseDropdownRow(
    label: String,
    options: List<Pair<T, String>>,
    selectedLabel: String,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = selectedLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (value, optionLabel) ->
                DropdownMenuItem(
                    text = { Text(optionLabel) },
                    onClick = {
                        onSelected(value)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ExpenseStringDropdownRow(
    placeholder: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selected.ifBlank { placeholder },
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected.isBlank()) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ExpenseProjectDropdown(
    projects: List<ProjectEntity>,
    projectId: String?,
    onSelected: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val noProject = stringResource(R.string.expenses_form_no_project)
    val selectedName = projects.firstOrNull { it.id == projectId }?.name ?: noProject
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(noProject) },
                onClick = {
                    onSelected(null)
                    expanded = false
                }
            )
            projects.forEach { project ->
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

@Composable
private fun ExpenseIosTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    prefix: String? = null,
    readOnly: Boolean = false
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
        prefix = if (prefix != null) {
            {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = prefix,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(4.dp))
                }
            }
        } else {
            null
        },
        singleLine = singleLine,
        minLines = minLines,
        maxLines = if (singleLine) 1 else Int.MAX_VALUE,
        keyboardOptions = keyboardOptions,
        readOnly = readOnly,
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

@Composable
private fun EmbeddedMultiDatePicker(
    selectedDates: List<Long>,
    initialMonthMillis: Long,
    onToggleDate: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var displayedMonthStart by remember(initialMonthMillis) { mutableStateOf(startOfMonth(initialMonthMillis)) }
    val calendar = remember { Calendar.getInstance() }
    val monthLabel = remember(displayedMonthStart) {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(displayedMonthStart))
    }

    val firstDayOfWeek = calendar.firstDayOfWeek
    val dayHeaders = listOf(
        Calendar.SUNDAY,
        Calendar.MONDAY,
        Calendar.TUESDAY,
        Calendar.WEDNESDAY,
        Calendar.THURSDAY,
        Calendar.FRIDAY,
        Calendar.SATURDAY
    ).let { days ->
        val startIdx = days.indexOf(firstDayOfWeek).coerceAtLeast(0)
        (days.drop(startIdx) + days.take(startIdx)).map { day ->
            calendar.set(Calendar.DAY_OF_WEEK, day)
            SimpleDateFormat("EE", Locale.getDefault()).format(calendar.time).take(2)
        }
    }

    val cells = remember(displayedMonthStart, firstDayOfWeek) {
        buildMonthCells(displayedMonthStart, firstDayOfWeek)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = {
                val cal = Calendar.getInstance().apply { timeInMillis = displayedMonthStart }
                cal.add(Calendar.MONTH, -1)
                displayedMonthStart = startOfMonth(cal.timeInMillis)
            }) {
                Text("<")
            }
            Text(text = monthLabel, style = MaterialTheme.typography.titleSmall)
            TextButton(onClick = {
                val cal = Calendar.getInstance().apply { timeInMillis = displayedMonthStart }
                cal.add(Calendar.MONTH, 1)
                displayedMonthStart = startOfMonth(cal.timeInMillis)
            }) {
                Text(">")
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            dayHeaders.forEach { header ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(text = header, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        cells.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { dateMillis ->
                    val selected = dateMillis != null && selectedDates.contains(dateMillis)
                    val backgroundColor = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(backgroundColor)
                            .clickable(enabled = dateMillis != null) {
                                if (dateMillis != null) {
                                    onToggleDate(dateMillis, selected)
                                }
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (dateMillis == null) "" else dayOfMonth(dateMillis).toString(),
                            color = if (selected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

private fun buildMonthCells(monthStartMillis: Long, firstDayOfWeek: Int): List<Long?> {
    val cal = Calendar.getInstance().apply { timeInMillis = monthStartMillis }
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstWeekDayOfMonth = cal.get(Calendar.DAY_OF_WEEK)
    val leadingCells = (firstWeekDayOfMonth - firstDayOfWeek + 7) % 7

    val cells = mutableListOf<Long?>()
    repeat(leadingCells) { cells.add(null) }

    for (day in 1..daysInMonth) {
        cal.set(Calendar.DAY_OF_MONTH, day)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cells.add(cal.timeInMillis)
    }

    while (cells.size % 7 != 0) {
        cells.add(null)
    }
    return cells
}

private fun startOfMonth(millis: Long): Long {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

private fun dayOfMonth(millis: Long): Int {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    return cal.get(Calendar.DAY_OF_MONTH)
}

@Composable
private fun expenseCategoryLabel(category: ExpenseCategory): String = when (category) {
    ExpenseCategory.MATERIALS -> stringResource(R.string.expenses_category_materials)
    ExpenseCategory.LABOR -> stringResource(R.string.expenses_category_labor)
    ExpenseCategory.EQUIPMENT -> stringResource(R.string.expenses_category_equipment)
    ExpenseCategory.MISC -> stringResource(R.string.expenses_category_misc)
}

@Composable
private fun laborTypeLabel(type: LaborType): String = when (type) {
    LaborType.HOURLY -> stringResource(R.string.expenses_form_labor_mode_hourly)
    LaborType.DAILY -> stringResource(R.string.expenses_form_labor_mode_daily)
    LaborType.SUBCONTRACTOR -> stringResource(R.string.labor_type_subcontractor)
}

@Composable
private fun DatePickerField(date: Long, onDateSelected: (Long) -> Unit) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
