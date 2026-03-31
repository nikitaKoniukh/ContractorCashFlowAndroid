package com.yetzira.ContractorCashFlowAndroid.ui.invoices

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yetzira.ContractorCashFlowAndroid.R
import com.yetzira.ContractorCashFlowAndroid.data.preferences.CurrencyOption
import com.yetzira.ContractorCashFlowAndroid.data.preferences.UserPreferencesRepository

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
    val context = LocalContext.current
    val preferencesRepository = remember(context) { UserPreferencesRepository(context.applicationContext) }
    val currency by preferencesRepository.selectedCurrencyCode.collectAsState(initial = CurrencyOption.ILS)

    LaunchedEffect(invoiceId) {
        viewModel.startEdit(invoiceId)
    }

    // Full reset when the invoice data first loads (invoiceId or core fields change).
    LaunchedEffect(vmState.invoiceId, vmState.dueDate, vmState.isPaid) {
        if (vmState.invoiceId != null) {
            formState = viewModel.updateForm(vmState)
        }
    }

    // Only refresh server-fetched lists; preserve any edits the user has made.
    LaunchedEffect(vmState.existingClients, vmState.projects) {
        formState = viewModel.updateForm(
            formState.copy(
                existingClients = vmState.existingClients,
                projects = vmState.projects
            )
        )
    }

    Scaffold(
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.invoices_screen_edit_title)) },
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
            currency = currency,
            onStateChange = { formState = viewModel.updateForm(it) },
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        )
    }
}


