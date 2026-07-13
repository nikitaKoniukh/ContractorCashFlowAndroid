package com.yetzira.ContractorCashFlowAndroid.ui.invoices

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yetzira.ContractorCashFlowAndroid.R
import com.yetzira.ContractorCashFlowAndroid.data.local.entity.ClientEntity
import com.yetzira.ContractorCashFlowAndroid.data.preferences.CurrencyOption
import com.yetzira.ContractorCashFlowAndroid.ui.components.formatAmountInput
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun InvoiceFormContent(
    state: InvoiceFormUiState,
    currency: CurrencyOption,
    onStateChange: (InvoiceFormUiState) -> Unit,
    modifier: Modifier = Modifier
) {
    val hasExistingClients = state.existingClients.isNotEmpty()
    val typedClientName = state.enteredClientName.trim()
    val duplicateTypedClient = !state.useExistingClient && typedClientName.isNotEmpty() &&
        state.existingClients.any { it.name.equals(typedClientName, ignoreCase = true) }
    val selectedClient = state.existingClients.firstOrNull {
        it.name.equals(state.selectedClientName, ignoreCase = true)
    }
    val warningOrange = Color(0xFFFF9500)

    LaunchedEffect(hasExistingClients) {
        if (!hasExistingClients && state.useExistingClient) {
            onStateChange(state.copy(useExistingClient = false))
        }
    }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            InvoiceFormSection(title = stringResource(R.string.invoices_section_details)) {
                if (hasExistingClients) {
                    ClientModeSegmentedControl(
                        useExistingClient = state.useExistingClient,
                        onModeSelected = { onStateChange(state.copy(useExistingClient = it)) },
                        modifier = Modifier.padding(16.dp)
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }

                if (state.useExistingClient && hasExistingClients) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        InvoiceClientDropdown(
                            options = state.existingClients,
                            selectedClientName = state.selectedClientName,
                            onSelected = { onStateChange(state.copy(selectedClientName = it)) }
                        )
                        if (selectedClient != null) {
                            val email = selectedClient.email?.trim().orEmpty()
                            val phone = selectedClient.phone?.trim().orEmpty()
                            if (email.isNotEmpty() || phone.isNotEmpty()) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 16.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (email.isNotEmpty()) {
                                        Text(
                                            text = email,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (phone.isNotEmpty()) {
                                        Text(
                                            text = phone,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    InvoiceIosTextField(
                        value = state.enteredClientName,
                        onValueChange = { onStateChange(state.copy(enteredClientName = it)) },
                        placeholder = stringResource(R.string.invoices_client_name),
                        singleLine = true
                    )
                }
            }
        }

        if (duplicateTypedClient) {
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = warningOrange
                    )
                    Text(
                        text = stringResource(R.string.invoices_duplicate_client_warning),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            InvoiceFormSection(title = stringResource(R.string.invoices_amount)) {
                InvoiceIosTextField(
                    value = state.amount,
                    onValueChange = { onStateChange(state.copy(amount = formatAmountInput(it))) },
                    placeholder = "0",
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    prefix = currency.symbol
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                DueDatePickerRow(
                    dueDate = state.dueDate,
                    onDateSelected = { onStateChange(state.copy(dueDate = it)) }
                )
            }
        }

        item {
            InvoiceFormSection(title = stringResource(R.string.invoices_project_optional)) {
                InvoiceProjectDropdown(
                    projects = state.projects,
                    projectId = state.projectId,
                    onSelected = { onStateChange(state.copy(projectId = it)) }
                )
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.invoices_paid_toggle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = state.isPaid,
                        onCheckedChange = { onStateChange(state.copy(isPaid = it)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun InvoiceFormSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title.uppercase(Locale.getDefault()),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp)
        )
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
private fun ClientModeSegmentedControl(
    useExistingClient: Boolean,
    onModeSelected: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            SegmentedControlItem(
                text = stringResource(R.string.invoices_client_mode_enter_name),
                selected = !useExistingClient,
                onClick = { onModeSelected(false) },
                modifier = Modifier.weight(1f)
            )
            SegmentedControlItem(
                text = stringResource(R.string.invoices_client_mode_select_existing),
                selected = useExistingClient,
                onClick = { onModeSelected(true) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SegmentedControlItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun InvoiceClientDropdown(
    options: List<ClientEntity>,
    selectedClientName: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selectedClientName.isBlank()) {
                    stringResource(R.string.invoices_select_client_prompt)
                } else {
                    selectedClientName
                },
                style = MaterialTheme.typography.bodyLarge,
                color = if (selectedClientName.isBlank()) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.invoices_select_client_prompt)) },
                onClick = {
                    onSelected("")
                    expanded = false
                }
            )
            options.forEach { client ->
                DropdownMenuItem(
                    text = { Text(client.name) },
                    onClick = {
                        onSelected(client.name)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun InvoiceProjectDropdown(
    projects: List<com.yetzira.ContractorCashFlowAndroid.data.local.entity.ProjectEntity>,
    projectId: String?,
    onSelected: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val noProject = stringResource(R.string.invoices_no_project)
    val selectedName = projects.firstOrNull { it.id == projectId }?.name ?: noProject
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(noProject) },
                onClick = {
                    onSelected(null)
                    expanded = false
                }
            )
            projects.forEach { project ->
                DropdownMenuItem(
                    text = { Text(project.name) },
                    onClick = {
                        onSelected(project.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun InvoiceIosTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    prefix: String? = null
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        prefix = if (prefix != null) {
            {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = prefix,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(4.dp))
                }
            }
        } else {
            null
        },
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        )
    )
}

@Composable
private fun DueDatePickerRow(dueDate: Long, onDateSelected: (Long) -> Unit) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.invoices_due_date),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        TextButton(onClick = {
            val cal = Calendar.getInstance().apply { timeInMillis = dueDate }
            DatePickerDialog(
                context,
                { _, year, month, day ->
                    val picked = Calendar.getInstance().apply {
                        set(year, month, day, 0, 0, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    onDateSelected(picked)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }) {
            Text(
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(dueDate)),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
