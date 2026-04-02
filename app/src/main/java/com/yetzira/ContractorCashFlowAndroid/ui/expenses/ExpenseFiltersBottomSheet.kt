package com.yetzira.ContractorCashFlowAndroid.ui.expenses

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
    var categoryMenu by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.expenses_filters_title),
                    style = MaterialTheme.typography.titleLarge
                )
                TextButton(onClick = { onApply(state) }) {
                    Text(text = stringResource(R.string.labor_filters_done))
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.expenses_filters_category_label),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FilterSelectorRow(
                    label = stringResource(R.string.expenses_filters_category_label),
                    value = state.category?.name ?: stringResource(R.string.expenses_filters_all),
                    onClick = { categoryMenu = true }
                )
                DropdownMenu(
                    expanded = categoryMenu,
                    onDismissRequest = { categoryMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.expenses_filters_all)) },
                        onClick = {
                            state = state.copy(category = null)
                            categoryMenu = false
                        }
                    )
                    ExpenseCategory.entries.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                state = state.copy(category = category)
                                categoryMenu = false
                            }
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.expenses_filter_start_date),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                DateRangeFilterCard(
                    startDate = state.startDate,
                    endDate = state.endDate,
                    onSelectionChanged = { start, end ->
                        state = state.copy(
                            hasStartDate = start != null,
                            startDate = start,
                            hasEndDate = end != null,
                            endDate = end
                        )
                    }
                )
            }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        state = ExpenseFilterState()
                        onClearAll()
                    }
                ) {
                    Text(text = stringResource(R.string.expenses_filters_clear))
                }
            }
        }
    }
}

@Composable
private fun DateRangeFilterCard(
    startDate: Long?,
    endDate: Long?,
    onSelectionChanged: (Long?, Long?) -> Unit
) {
    var displayedMonthStart by remember(startDate, endDate) {
        mutableStateOf(startOfMonth(startDate ?: endDate ?: System.currentTimeMillis()))
    }
    val calendar = remember { Calendar.getInstance() }
    val firstDayOfWeek = calendar.firstDayOfWeek
    val monthLabel = remember(displayedMonthStart) {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(displayedMonthStart))
    }
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

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            startDate != null && endDate != null -> "${formatDate(startDate)} - ${formatDate(endDate)}"
                            startDate != null -> "${formatDate(startDate)} - ${stringResource(R.string.expenses_filter_pick_date)}"
                            else -> stringResource(R.string.expenses_filter_pick_date)
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                TextButton(onClick = { onSelectionChanged(null, null) }) {
                    Text(stringResource(R.string.expenses_filters_clear))
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = {
                    val cal = Calendar.getInstance().apply { timeInMillis = displayedMonthStart }
                    cal.add(Calendar.MONTH, -1)
                    displayedMonthStart = startOfMonth(cal.timeInMillis)
                }) { Text("<") }
                Text(text = monthLabel, style = MaterialTheme.typography.titleSmall)
                TextButton(onClick = {
                    val cal = Calendar.getInstance().apply { timeInMillis = displayedMonthStart }
                    cal.add(Calendar.MONTH, 1)
                    displayedMonthStart = startOfMonth(cal.timeInMillis)
                }) { Text(">") }
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
                        val isSelectedStart = dateMillis != null && startDate == dateMillis
                        val isSelectedEnd = dateMillis != null && endDate == dateMillis
                        val inRange = if (dateMillis != null && startDate != null && endDate != null) {
                            dateMillis in startDate..endDate
                        } else {
                            false
                        }

                        val bgColor = when {
                            isSelectedStart || isSelectedEnd -> MaterialTheme.colorScheme.primary
                            inRange -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        val textColor = when {
                            isSelectedStart || isSelectedEnd -> MaterialTheme.colorScheme.onPrimary
                            else -> MaterialTheme.colorScheme.onSurface
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(2.dp)
                                .background(bgColor, RoundedCornerShape(6.dp))
                                .clickable(enabled = dateMillis != null) {
                                    if (dateMillis == null) return@clickable
                                    when {
                                        startDate == null || endDate != null -> {
                                            onSelectionChanged(dateMillis, null)
                                        }
                                        else -> {
                                            val start = startDate ?: return@clickable
                                            if (dateMillis >= start) {
                                                onSelectionChanged(start, dateMillis)
                                            } else {
                                                onSelectionChanged(dateMillis, start)
                                            }
                                        }
                                    }
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (dateMillis == null) "" else dayOfMonth(dateMillis).toString(),
                                color = textColor,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSelectorRow(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))

