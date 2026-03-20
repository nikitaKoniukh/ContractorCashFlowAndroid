package com.yetzira.ContractorCashFlowAndroid.ui.clients

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yetzira.ContractorCashFlowAndroid.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewClientScreen(
    viewModel: ClientViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var formState by remember { mutableStateOf(ClientFormUiState()) }

    LaunchedEffect(Unit) {
        viewModel.setOriginalClient(null)
        formState = viewModel.updateForm(ClientFormUiState())
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.clients_new)) },
                navigationIcon = { TextButton(onClick = onBack) { Text(stringResource(R.string.common_back)) } },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveClient(formState, onDone = onBack) },
                        enabled = formState.canSave
                    ) {
                        Text(stringResource(R.string.common_save))
                    }
                }
            )
        }
    ) { innerPadding ->
        ClientForm(
            state = formState,
            onChange = { formState = viewModel.updateForm(it) },
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        )
    }
}

@Composable
internal fun ClientForm(
    state: ClientFormUiState,
    onChange: (ClientFormUiState) -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
    ) {
        TextField(
            value = state.name,
            onValueChange = { onChange(state.copy(name = it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.clients_name)) },
            singleLine = true
        )
        TextField(
            value = state.email,
            onValueChange = { onChange(state.copy(email = it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.clients_email)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )
        TextField(
            value = state.phone,
            onValueChange = { onChange(state.copy(phone = it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.clients_phone)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true
        )
        TextField(
            value = state.address,
            onValueChange = { onChange(state.copy(address = it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.clients_address)) },
            minLines = 3,
            maxLines = 5
        )
        TextField(
            value = state.notes,
            onValueChange = { onChange(state.copy(notes = it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.clients_notes)) },
            minLines = 4,
            maxLines = 8
        )
    }
}


