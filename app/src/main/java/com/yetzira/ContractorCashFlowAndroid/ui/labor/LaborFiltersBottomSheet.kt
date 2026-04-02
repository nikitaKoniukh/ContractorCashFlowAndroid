package com.yetzira.ContractorCashFlowAndroid.ui.labor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yetzira.ContractorCashFlowAndroid.R
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.LaborType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaborFiltersBottomSheet(
    initial: LaborFiltersState,
    availableProjects: List<String>,
    availableMonths: List<LaborMonthOption>,
    onDone: (LaborFiltersState) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    var state by remember(initial, availableMonths) {
        mutableStateOf(
            if (initial.monthEnabled && initial.month == null && availableMonths.isNotEmpty()) {
                initial.copy(month = availableMonths.first())
            } else {
                initial
            }
        )
    }
    var laborTypeMenu by remember { mutableStateOf(false) }
    var projectMenu by remember { mutableStateOf(false) }

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.labor_filters_sheet_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = {
                    val normalizedState = if (
                        state.monthEnabled &&
                        state.month == null &&
                        availableMonths.isNotEmpty()
                    ) {
                        state.copy(month = availableMonths.first())
                    } else {
                        state
                    }
                    onDone(normalizedState)
                }) {
                    Text(text = stringResource(R.string.labor_filters_done))
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.labor_filters_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FilterSelectorRow(
                    label = stringResource(R.string.labor_filters_type_label),
                    value = state.laborType?.let { laborTypeLabel(it) }
                        ?: stringResource(R.string.labor_filters_all_types),
                    onClick = { laborTypeMenu = true }
                )
                DropdownMenu(
                    expanded = laborTypeMenu,
                    onDismissRequest = { laborTypeMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.labor_filters_all_types)) },
                        onClick = {
                            state = state.copy(laborType = null)
                            laborTypeMenu = false
                        }
                    )
                    LaborType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(laborTypeLabel(type)) },
                            onClick = {
                                state = state.copy(laborType = type)
                                laborTypeMenu = false
                            }
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.labor_filters_project_section),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FilterSelectorRow(
                    label = stringResource(R.string.labor_filters_project_label),
                    value = state.projectName ?: stringResource(R.string.labor_filters_all_projects),
                    onClick = { projectMenu = true }
                )
                DropdownMenu(expanded = projectMenu, onDismissRequest = { projectMenu = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.labor_filters_all_projects)) },
                        onClick = {
                            state = state.copy(projectName = null)
                            projectMenu = false
                        }
                    )
                    availableProjects.forEach { project ->
                        DropdownMenuItem(
                            text = { Text(project) },
                            onClick = {
                                state = state.copy(projectName = project)
                                projectMenu = false
                            }
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.labor_filters_month_section),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.labor_filters_by_month),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Switch(
                                checked = state.monthEnabled,
                                onCheckedChange = { checked ->
                                    state = state.copy(
                                        monthEnabled = checked,
                                        month = if (checked) state.month ?: availableMonths.firstOrNull() else null
                                    )
                                }
                            )
                        }

                        if (state.monthEnabled && availableMonths.isNotEmpty()) {
                            HorizontalDivider(
                                modifier = Modifier.padding(top = 10.dp, bottom = 4.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            WheelMonthPicker(
                                items = availableMonths,
                                selectedItem = state.month,
                                onItemSelected = { selected ->
                                    state = state.copy(month = selected)
                                }
                            )
                        }
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        state = LaborFiltersState()
                        onClearAll()
                    }
                ) {
                    Text(text = stringResource(R.string.labor_filters_clear_button))
                }
            }
        }
    }
}

// ── Wheel picker ─────────────────────────────────────────────────────────────

private const val WHEEL_VISIBLE_ITEMS = 5

@Composable
private fun WheelMonthPicker(
    items: List<LaborMonthOption>,
    selectedItem: LaborMonthOption?,
    onItemSelected: (LaborMonthOption) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemHeightDp = 48.dp
    val density = LocalDensity.current
    val itemHeightPx = with(density) { itemHeightDp.toPx() }
    val halfCount = WHEEL_VISIBLE_ITEMS / 2 // 2

    val initialIndex = items.indexOfFirst { it == selectedItem }.coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val snapBehavior = rememberSnapFlingBehavior(listState)

    // Index of the item currently centred in the wheel
    val centeredIndex by remember(listState) {
        derivedStateOf {
            val fvi = listState.firstVisibleItemIndex
            val offset = listState.firstVisibleItemScrollOffset
            (if (offset > itemHeightPx / 2) fvi + 1 else fvi).coerceIn(0, items.lastIndex)
        }
    }

    LaunchedEffect(centeredIndex) {
        onItemSelected(items[centeredIndex])
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(itemHeightDp * WHEEL_VISIBLE_ITEMS)
    ) {
        // Scrollable list
        LazyColumn(
            state = listState,
            flingBehavior = snapBehavior,
            modifier = Modifier.fillMaxSize()
        ) {
            // Top padding so first real item can sit in the centre
            items(halfCount, key = { "pad_top_$it" }) {
                Box(modifier = Modifier.height(itemHeightDp))
            }
            items(items.size, key = { "month_$it" }) { index ->
                val isSelected = index == centeredIndex
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeightDp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = items[index].label,
                        style = if (isSelected)
                            MaterialTheme.typography.titleMedium
                        else
                            MaterialTheme.typography.bodyMedium,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
            // Bottom padding so last real item can sit in the centre
            items(halfCount, key = { "pad_bot_$it" }) {
                Box(modifier = Modifier.height(itemHeightDp))
            }
        }

        // Selection highlight drawn behind the centre row
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(itemHeightDp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(12.dp)
                )
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

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
            verticalAlignment = Alignment.CenterVertically
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

@Composable
private fun laborTypeLabel(type: LaborType): String {
    return when (type) {
        LaborType.HOURLY -> stringResource(R.string.labor_type_hourly)
        LaborType.DAILY -> stringResource(R.string.labor_type_daily)
        LaborType.CONTRACT -> stringResource(R.string.labor_type_contract)
        LaborType.SUBCONTRACTOR -> stringResource(R.string.labor_type_subcontractor)
    }
}
