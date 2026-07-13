package com.yetzira.ContractorCashFlowAndroid.ui.invoices

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.yetzira.ContractorCashFlowAndroid.ui.navigation.KablanProLayoutDefaults
import com.yetzira.ContractorCashFlowAndroid.ui.navigation.KablanProTopBar

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
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(invoiceId) {
        viewModel.startEdit(invoiceId)
    }

    LaunchedEffect(vmState.invoiceId, vmState.dueDate, vmState.isPaid) {
        if (vmState.invoiceId != null) {
            formState = viewModel.updateForm(vmState)
        }
    }

    LaunchedEffect(vmState.existingClients, vmState.projects) {
        formState = viewModel.updateForm(
            formState.copy(
                existingClients = vmState.existingClients,
                projects = vmState.projects
            )
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.common_delete)) },
            text = { Text(stringResource(R.string.invoices_delete_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteById(invoiceId, onDone = onBack)
                }) {
                    Text(stringResource(R.string.common_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier.fillMaxSize(),
        topBar = {
            KablanProTopBar(
                title = stringResource(R.string.invoices_screen_edit_title),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                },
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
                onClick = { showDeleteDialog = true },
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
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                .padding(top = KablanProLayoutDefaults.TopSectionSpacing)
        )
    }
}
