package com.yetzira.ContractorCashFlowAndroid.ui.clients

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yetzira.ContractorCashFlowAndroid.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDetailScreen(
    clientId: String,
    viewModel: ClientViewModel,
    onEdit: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val detailState by viewModel.detailUiState.collectAsState()

    LaunchedEffect(clientId) {
        viewModel.selectClient(clientId)
    }

    val client = detailState.client

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.clients_screen_detail_title)) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text(stringResource(R.string.common_back)) }
                },
                actions = {
                    if (client != null) {
                        TextButton(onClick = { onEdit(client.id) }) {
                            Text(stringResource(R.string.common_edit))
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (client == null) {
            Text(
                text = "Client not found",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp)
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ContactRow(Icons.Default.Person, client.name)
            if (!client.email.isNullOrBlank()) {
                ContactRow(Icons.Default.Email, client.email)
            }
            if (!client.phone.isNullOrBlank()) {
                ContactRow(Icons.Default.Phone, client.phone)
            }
            if (!client.address.isNullOrBlank()) {
                ContactRow(Icons.Default.LocationOn, client.address)
            }

            if (!client.notes.isNullOrBlank()) {
                Text(
                    text = "Notes",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 10.dp)
                )
                Text(text = client.notes)
            }
        }
    }
}

@Composable
private fun ContactRow(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.Icon(
            imageVector = icon,
            contentDescription = null
        )
        Text(text = value)
    }
}

@Composable
fun ClientDetailScreen(
    clientName: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Client Details", style = MaterialTheme.typography.titleMedium)
        Text(text = clientName, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 10.dp))
        TextButton(onClick = onBack, modifier = Modifier.padding(top = 12.dp)) {
            Text(stringResource(R.string.common_back))
        }
    }
}

