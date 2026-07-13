package com.yetzira.ContractorCashFlowAndroid.ui.clients

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.yetzira.ContractorCashFlowAndroid.ui.navigation.KablanProTopBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yetzira.ContractorCashFlowAndroid.R
import com.yetzira.ContractorCashFlowAndroid.ui.navigation.KablanProLayoutDefaults

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
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier.fillMaxSize(),
        topBar = {
            KablanProTopBar(
                title = stringResource(R.string.clients_new),
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
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                .padding(top = KablanProLayoutDefaults.TopSectionSpacing)
        )
    }
}
