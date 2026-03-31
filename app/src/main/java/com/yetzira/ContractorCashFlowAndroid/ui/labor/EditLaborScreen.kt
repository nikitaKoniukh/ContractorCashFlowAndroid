package com.yetzira.ContractorCashFlowAndroid.ui.labor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yetzira.ContractorCashFlowAndroid.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLaborScreen(
    workerId: String,
    viewModel: LaborViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState by viewModel.listUiState.collectAsState()
    val detailState by viewModel.detailUiState.collectAsState()
    var formState by remember { mutableStateOf(LaborFormUiState()) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(workerId) {
        viewModel.selectWorker(workerId)
    }

    LaunchedEffect(detailState.worker?.id) {
        viewModel.setOriginalWorker(detailState.worker)
        formState = viewModel.updateForm(
            viewModel.buildFormState(detailState.worker),
            listState.workers.map { it.worker }
        )
    }

    val worker = detailState.worker
    val metrics = detailState.metrics

    Scaffold(
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.labor_screen_edit_title)) },
                navigationIcon = { TextButton(onClick = onBack) { Text(stringResource(R.string.action_back)) } },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveWorker(formState, onDone = onBack) },
                        enabled = formState.canSave
                    ) { Text(stringResource(R.string.action_save)) }
                }
            )
        },
        bottomBar = {
            TextButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.padding(8.dp)
            ) { Text(stringResource(R.string.action_delete)) }
        }
    ) { innerPadding ->
        if (worker == null) {
            Text(text = "Worker not found", modifier = Modifier.padding(innerPadding).padding(24.dp))
            return@Scaffold
        }

        Column(
            modifier = Modifier.padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LaborFormContent(
                state = formState,
                onChange = { formState = viewModel.updateForm(it, listState.workers.map { metric -> metric.worker }) }
            )

            if (metrics != null && metrics.linkedExpenseCount > 0) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Worker Statistics")
                    Text("Total earned: ${String.format(java.util.Locale.US, "%.2f", metrics.totalAmountEarned)}")
                    Text("Hours/Units: ${String.format(java.util.Locale.US, "%.2f", metrics.totalUnitsWorked)}")
                    Text("Days worked: ${metrics.totalDaysWorked}")
                    if (metrics.associatedProjects.isNotEmpty()) {
                        Text("Projects: ${metrics.associatedProjects.joinToString()}")
                    }
                }
            }
        }
    }

    if (showDeleteDialog && worker != null) {
        val linkedCount = metrics?.linkedExpenseCount ?: 0
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete worker?") },
            text = {
                Text(
                    if (linkedCount > 0) {
                        "$linkedCount linked expense(s) will remain but unlinked."
                    } else {
                        "This worker will be deleted."
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteWorker(worker) { onBack() }
                    showDeleteDialog = false
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

