package com.yetzira.ContractorCashFlowAndroid.ui.labor

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLaborScreen(
    viewModel: LaborViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState by viewModel.listUiState.collectAsState()
    var formState by remember { mutableStateOf(LaborFormUiState()) }

    LaunchedEffect(Unit) {
        viewModel.setOriginalWorker(null)
        formState = viewModel.updateForm(LaborFormUiState(), listState.workers.map { it.worker })
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Add Worker") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveWorker(formState, onDone = onBack) },
                        enabled = formState.canSave
                    ) { Text("Save") }
                }
            )
        }
    ) { innerPadding ->
        LaborFormContent(
            state = formState,
            onChange = { formState = viewModel.updateForm(it, listState.workers.map { worker -> worker.worker }) },
            modifier = Modifier.padding(innerPadding).padding(16.dp)
        )
    }
}

