package com.yetzira.ContractorCashFlowAndroid.ui.projects

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.yetzira.ContractorCashFlowAndroid.data.preferences.UserPreferencesRepository
import com.yetzira.ContractorCashFlowAndroid.ui.components.formatAmountInput
import com.yetzira.ContractorCashFlowAndroid.ui.components.parseAmountInput
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewProjectScreen(
    viewModel: ProjectViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val preferencesRepository = remember(context) { UserPreferencesRepository(context.applicationContext) }
    val currency by preferencesRepository.selectedCurrencyCode.collectAsState(initial = CurrencyOption.ILS)
    val existingClients by viewModel.existingClients.collectAsState()

    var name by rememberSaveable { mutableStateOf("") }
    var useExistingClient by rememberSaveable { mutableStateOf(false) }
    var selectedClientName by rememberSaveable { mutableStateOf("") }
    var clientName by rememberSaveable { mutableStateOf("") }
    var budget by rememberSaveable { mutableStateOf("") }
    var isActive by rememberSaveable { mutableStateOf(true) }
    var showClientDetails by rememberSaveable { mutableStateOf(false) }
    var newClientEmail by rememberSaveable { mutableStateOf("") }
    var newClientPhone by rememberSaveable { mutableStateOf("") }
    var newClientAddress by rememberSaveable { mutableStateOf("") }
    var newClientNotes by rememberSaveable { mutableStateOf("") }

    val hasExistingClients = existingClients.isNotEmpty()
    val normalizedProjectName = name.trim()
    val normalizedTypedClientName = clientName.trim()
    val duplicateTypedClient = !useExistingClient && normalizedTypedClientName.isNotEmpty() &&
        existingClients.any { it.name.equals(normalizedTypedClientName, ignoreCase = true) }
    val finalClientName = if (useExistingClient) selectedClientName.trim() else normalizedTypedClientName
    val budgetValue = parseAmountInput(budget) ?: 0.0
    val canSave = normalizedProjectName.isNotEmpty() && finalClientName.isNotEmpty() && budgetValue > 0.0
    val bgColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val showNewClientInfoSection = !useExistingClient && normalizedTypedClientName.isNotEmpty() && !duplicateTypedClient

    LaunchedEffect(hasExistingClients) {
        if (!hasExistingClients) useExistingClient = false
    }
    LaunchedEffect(showNewClientInfoSection) {
        if (!showNewClientInfoSection) showClientDetails = false
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier.fillMaxSize(),
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.projects_new_project),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
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
                        onClick = {
                            viewModel.createProject(
                                name = normalizedProjectName,
                                budgetText = budget,
                                useExistingClient = useExistingClient,
                                selectedClientName = selectedClientName.trim(),
                                newClientName = normalizedTypedClientName,
                                newClientEmail = newClientEmail,
                                newClientPhone = newClientPhone,
                                newClientAddress = newClientAddress,
                                newClientNotes = newClientNotes,
                                notes = "",
                                isActive = isActive,
                                onSuccess = onBack
                            )
                        },
                        enabled = canSave
                    ) {
                        Text(
                            text = stringResource(R.string.common_save),
                            color = if (canSave) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .padding(innerPadding)
        ) {
            val isTablet = maxWidth >= 600.dp
            Column(
                modifier = Modifier
                    .then(if (isTablet) Modifier.widthIn(max = 600.dp) else Modifier.fillMaxWidth())
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                FormSection(title = stringResource(R.string.projects_info_section)) {
                    IosTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = stringResource(R.string.projects_name),
                        singleLine = true
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    if (hasExistingClients) {
                        ClientModeSegmentedControl(
                            useExistingClient = useExistingClient,
                            onModeSelected = { useExistingClient = it },
                            modifier = Modifier.padding(16.dp)
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }

                    if (useExistingClient && hasExistingClients) {
                        IosDropdownField(
                            options = existingClients,
                            selectedClientName = selectedClientName,
                            onSelected = { selectedClientName = it },
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    } else {
                        IosTextField(
                            value = clientName,
                            onValueChange = { clientName = it },
                            placeholder = stringResource(R.string.projects_client_name),
                            singleLine = true
                        )
                    }
                }

                if (duplicateTypedClient) {
                    Text(
                        text = stringResource(R.string.projects_duplicate_client_warning),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                if (showNewClientInfoSection) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.clients_info_section).uppercase(Locale.getDefault()),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 16.dp, bottom = 2.dp)
                        )
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showClientDetails = !showClientDetails }
                                        .padding(horizontal = 16.dp, vertical = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = stringResource(R.string.projects_new_client_details_optional),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Icon(
                                        imageVector = if (showClientDetails) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                AnimatedVisibility(visible = showClientDetails) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(start = 16.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        )
                                        IosTextField(
                                            value = newClientEmail,
                                            onValueChange = { newClientEmail = it },
                                            placeholder = stringResource(R.string.projects_client_email),
                                            singleLine = true
                                        )
                                        HorizontalDivider(
                                            modifier = Modifier.padding(start = 16.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        )
                                        IosTextField(
                                            value = newClientPhone,
                                            onValueChange = { newClientPhone = it },
                                            placeholder = stringResource(R.string.projects_client_phone),
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                                        )
                                        HorizontalDivider(
                                            modifier = Modifier.padding(start = 16.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        )
                                        IosTextField(
                                            value = newClientAddress,
                                            onValueChange = { newClientAddress = it },
                                            placeholder = stringResource(R.string.projects_client_address),
                                            singleLine = false,
                                            minLines = 2,
                                            maxLines = 4
                                        )
                                        HorizontalDivider(
                                            modifier = Modifier.padding(start = 16.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        )
                                        IosTextField(
                                            value = newClientNotes,
                                            onValueChange = { newClientNotes = it },
                                            placeholder = stringResource(R.string.projects_client_notes),
                                            singleLine = false,
                                            minLines = 2,
                                            maxLines = 4
                                        )
                                    }
                                }
                            }
                        }
                        Text(
                            text = stringResource(R.string.projects_new_client_footer),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }

                FormSection(title = stringResource(R.string.projects_budget)) {
                    IosTextField(
                        value = budget,
                        onValueChange = { budget = formatAmountInput(it) },
                        placeholder = "0",
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        prefix = currency.symbol
                    )
                }

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
                            text = stringResource(R.string.projects_active),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = isActive,
                            onCheckedChange = { isActive = it }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FormSection(
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
                text = stringResource(R.string.projects_client_mode_enter_name),
                selected = !useExistingClient,
                onClick = { onModeSelected(false) },
                modifier = Modifier.weight(1f)
            )
            SegmentedControlItem(
                text = stringResource(R.string.projects_client_mode_select_existing),
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
private fun IosDropdownField(
    options: List<ClientEntity>,
    selectedClientName: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selectedClientName.isBlank()) stringResource(R.string.projects_select_client_prompt) else selectedClientName,
                style = MaterialTheme.typography.bodyLarge,
                color = if (selectedClientName.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.projects_select_client_prompt)) },
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
private fun IosTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
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
        } else null,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        keyboardOptions = keyboardOptions,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        )
    )
}
