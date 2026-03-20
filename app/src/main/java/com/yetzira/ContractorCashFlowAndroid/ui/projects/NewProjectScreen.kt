package com.yetzira.ContractorCashFlowAndroid.ui.projects

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yetzira.ContractorCashFlowAndroid.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewProjectScreen(
    viewModel: ProjectViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clients by viewModel.existingClients.collectAsState()

    var name by rememberSaveable { mutableStateOf("") }
    var budget by rememberSaveable { mutableStateOf("") }
    var useExistingClient by rememberSaveable { mutableStateOf(true) }
    var selectedClientName by rememberSaveable { mutableStateOf("") }
    var newClientName by rememberSaveable { mutableStateOf("") }
    var newClientEmail by rememberSaveable { mutableStateOf("") }
    var newClientPhone by rememberSaveable { mutableStateOf("") }
    var newClientAddress by rememberSaveable { mutableStateOf("") }
    var newClientNotes by rememberSaveable { mutableStateOf("") }

    val clientName = if (useExistingClient) selectedClientName else newClientName
    val budgetValue = budget.toDoubleOrNull() ?: 0.0
    val canSave = name.isNotBlank() && clientName.isNotBlank() && budgetValue > 0.0
    val duplicateClient = !useExistingClient && clients.any { it.name.equals(newClientName, ignoreCase = true) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.projects_new_project)) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text(stringResource(R.string.common_back)) }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.createProject(
                                name = name,
                                budgetText = budget,
                                useExistingClient = useExistingClient,
                                selectedClientName = selectedClientName,
                                newClientName = newClientName,
                                newClientEmail = newClientEmail,
                                newClientPhone = newClientPhone,
                                newClientAddress = newClientAddress,
                                newClientNotes = newClientNotes,
                                onSuccess = onBack
                            )
                        },
                        enabled = canSave
                    ) {
                        Text(stringResource(R.string.common_save))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.projects_name)) },
                singleLine = true
            )

            TextField(
                value = budget,
                onValueChange = { budget = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.projects_budget)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.projects_use_existing_client))
                Switch(checked = useExistingClient, onCheckedChange = { useExistingClient = it })
            }

            if (useExistingClient) {
                ClientDropdown(
                    clients = clients.map { it.name },
                    selected = selectedClientName,
                    onSelect = { selectedClientName = it }
                )
            } else {
                TextField(
                    value = newClientName,
                    onValueChange = { newClientName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.projects_client_name)) },
                    singleLine = true
                )

                if (duplicateClient) {
                    Text(
                        text = stringResource(R.string.projects_duplicate_client_warning),
                        color = Color(0xFFFF9500),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                AnimatedVisibility(visible = true) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextField(
                            value = newClientEmail,
                            onValueChange = { newClientEmail = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.projects_client_email)) },
                            singleLine = true
                        )
                        TextField(
                            value = newClientPhone,
                            onValueChange = { newClientPhone = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.projects_client_phone)) },
                            singleLine = true
                        )
                        TextField(
                            value = newClientAddress,
                            onValueChange = { newClientAddress = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.projects_client_address)) },
                            singleLine = true
                        )
                        TextField(
                            value = newClientNotes,
                            onValueChange = { newClientNotes = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.projects_client_notes)) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClientDropdown(
    clients: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        TextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            label = { Text(stringResource(R.string.projects_select_client)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            clients.forEach { client ->
                DropdownMenuItem(
                    text = { Text(client) },
                    onClick = {
                        onSelect(client)
                        expanded = false
                    }
                )
            }
        }
    }
}


