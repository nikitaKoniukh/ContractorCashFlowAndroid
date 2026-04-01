package com.yetzira.ContractorCashFlowAndroid.ui.labor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.unit.dp
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
    var state by remember { mutableStateOf(initial) }
    var projectMenu by remember { mutableStateOf(false) }
    var monthMenu by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Worker Filters", style = MaterialTheme.typography.titleMedium)

            Text(text = "Worker type")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.laborType == null,
                    onClick = { state = state.copy(laborType = null) },
                    label = { Text("All") }
                )
                LaborType.entries.forEach { type ->
                    FilterChip(
                        selected = state.laborType == type,
                        onClick = { state = state.copy(laborType = type) },
                        label = { Text(type.name) }
                    )
                }
            }

            BoxSelector(
                title = "Project",
                value = state.projectName ?: "All Projects",
                onClick = { projectMenu = true }
            )
            DropdownMenu(expanded = projectMenu, onDismissRequest = { projectMenu = false }) {
                DropdownMenuItem(text = { Text("All Projects") }, onClick = {
                    state = state.copy(projectName = null)
                    projectMenu = false
                })
                availableProjects.forEach { project ->
                    DropdownMenuItem(text = { Text(project) }, onClick = {
                        state = state.copy(projectName = project)
                        projectMenu = false
                    })
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Filter by month")
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

            if (state.monthEnabled) {
                BoxSelector(
                    title = "Month",
                    value = state.month?.label ?: "Select month",
                    onClick = { monthMenu = true }
                )
                DropdownMenu(expanded = monthMenu, onDismissRequest = { monthMenu = false }) {
                    availableMonths.forEach { month ->
                        DropdownMenuItem(text = { Text(month.label) }, onClick = {
                            state = state.copy(month = month)
                            monthMenu = false
                        })
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onClearAll) { Text("Clear All") }
                TextButton(onClick = { onDone(state) }) { Text("Done") }
            }
        }
    }
}

@Composable
private fun BoxSelector(title: String, value: String, onClick: () -> Unit) {
    Column {
        Text(text = title)
        TextButton(onClick = onClick) { Text(value) }
    }
}

