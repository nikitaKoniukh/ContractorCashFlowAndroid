package com.yetzira.ContractorCashFlowAndroid.ui.invoices

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yetzira.ContractorCashFlowAndroid.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditInvoiceScreen(
    invoiceId: String,
    viewModel: InvoiceViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val vmState by viewModel.formUiState.collectAsState()
    var formState by remember { mutableStateOf(vmState) }

    LaunchedEffect(invoiceId) {
        viewModel.startEdit(invoiceId)
    }

    LaunchedEffect(vmState.invoiceId, vmState.existingClients, vmState.projects, vmState.dueDate, vmState.isPaid) {
        formState = viewModel.updateForm(vmState)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Edit Invoice") },
                navigationIcon = { TextButton(onClick = onBack) { Text(stringResource(R.string.common_back)) } },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveInvoice(formState, onDone = onBack) },
                        enabled = formState.canSave
                    ) {
                        Text(stringResource(R.string.common_save))
                    }
                }
            )
        },
        bottomBar = {
            TextButton(
                onClick = { viewModel.deleteById(invoiceId, onDone = onBack) },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(stringResource(R.string.common_delete))
            }
        }
    ) { innerPadding ->
        InvoiceFormContent(
            state = formState,
            onStateChange = { formState = viewModel.updateForm(it) },
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        )
    }
}


