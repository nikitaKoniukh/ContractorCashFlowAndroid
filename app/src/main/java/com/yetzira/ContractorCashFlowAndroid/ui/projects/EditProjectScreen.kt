package com.yetzira.ContractorCashFlowAndroid.ui.projects

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yetzira.ContractorCashFlowAndroid.R
import com.yetzira.ContractorCashFlowAndroid.data.preferences.CurrencyOption
import com.yetzira.ContractorCashFlowAndroid.data.preferences.UserPreferencesRepository
import com.yetzira.ContractorCashFlowAndroid.ui.components.ModernDropdown
import com.yetzira.ContractorCashFlowAndroid.ui.components.ModernTextField
import com.yetzira.ContractorCashFlowAndroid.ui.components.formatAmountInput
import com.yetzira.ContractorCashFlowAndroid.ui.components.formatCurrencyAmount
import com.yetzira.ContractorCashFlowAndroid.ui.components.parseAmountInput
import com.yetzira.ContractorCashFlowAndroid.ui.navigation.KablanProLayoutDefaults
import com.yetzira.ContractorCashFlowAndroid.ui.navigation.KablanProTopBar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProjectScreen(
    projectId: String,
    viewModel: ProjectViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.detailUiState.collectAsState()
    val existingClients by viewModel.existingClients.collectAsState()
    val context = LocalContext.current
    val preferencesRepository = remember(context) { UserPreferencesRepository(context.applicationContext) }
    val currency by preferencesRepository.selectedCurrencyCode.collectAsState(initial = CurrencyOption.ILS)

    LaunchedEffect(projectId) {
        viewModel.selectProject(projectId)
    }

    val project = state.project
    if (project == null) {
        Text(stringResource(R.string.projects_not_found))
        return
    }

    var name by remember(project.id) { mutableStateOf(project.name) }
    var clientName by remember(project.id) { mutableStateOf(project.clientName) }
    var budget by remember(project.id) {
        mutableStateOf(
            if (project.budget > 0) formatAmountInput(project.budget.toLong().toString()) else ""
        )
    }
    var isActive by remember(project.id) { mutableStateOf(project.isActive) }
    var hasExpectedCompletion by remember(project.id) { mutableStateOf(project.endDate != null) }
    var expectedEndDate by remember(project.id) { mutableStateOf(project.endDate) }

    val clientOptions = remember(existingClients, clientName) {
        val cleaned = existingClients
            .map { it.name.trim() }
            .filter { it.isNotBlank() }
            .distinct()
        if (clientName.isNotBlank() && cleaned.none { it.equals(clientName, ignoreCase = true) }) {
            listOf(clientName) + cleaned
        } else {
            cleaned
        }
    }

    val budgetValue = parseAmountInput(budget) ?: 0.0
    val reducedBelowExpenses = budgetValue in 0.0..<state.totalExpenses
    val resolvedEndDate = if (hasExpectedCompletion) {
        expectedEndDate ?: startOfDayMillis(System.currentTimeMillis())
    } else {
        null
    }
    val isValid = name.isNotBlank() && clientName.isNotBlank() && budgetValue > 0.0 &&
        (!hasExpectedCompletion || resolvedEndDate != null)
    val hasChanges = name != project.name ||
        clientName != project.clientName ||
        abs(budgetValue - project.budget) > 0.0001 ||
        isActive != project.isActive ||
        resolvedEndDate != project.endDate
    val canSave = isValid && hasChanges

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier.fillMaxSize(),
        topBar = {
            KablanProTopBar(
                title = stringResource(R.string.common_edit),
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
                            viewModel.updateProject(
                                project.copy(
                                    name = name.trim(),
                                    clientName = clientName.trim(),
                                    budget = budgetValue,
                                    endDate = resolvedEndDate,
                                    isActive = isActive
                                )
                            )
                            onBack()
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                .padding(top = KablanProLayoutDefaults.TopSectionSpacing)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModernTextField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(R.string.projects_name),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            ModernTextField(
                value = clientName,
                onValueChange = { clientName = it },
                label = stringResource(R.string.projects_client_name),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (clientOptions.isNotEmpty()) {
                ModernDropdown(
                    label = stringResource(R.string.projects_select_client),
                    options = clientOptions,
                    selected = clientName,
                    onSelected = { selected -> clientName = selected },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            ModernTextField(
                value = budget,
                onValueChange = { budget = formatAmountInput(it) },
                label = stringResource(R.string.projects_budget),
                modifier = Modifier.fillMaxWidth(),
                suffix = { Text(currency.symbol) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            if (reducedBelowExpenses) {
                Text(
                    text = stringResource(R.string.projects_budget_reduction_warning),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.projects_expected_completion),
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = hasExpectedCompletion,
                    onCheckedChange = { enabled ->
                        hasExpectedCompletion = enabled
                        expectedEndDate = if (enabled) {
                            expectedEndDate ?: startOfDayMillis(System.currentTimeMillis())
                        } else {
                            null
                        }
                    }
                )
            }

            AnimatedVisibility(visible = hasExpectedCompletion) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    EditEndDatePickerRow(
                        endDate = expectedEndDate ?: startOfDayMillis(System.currentTimeMillis()),
                        onDateSelected = { expectedEndDate = it }
                    )
                    Text(
                        text = stringResource(R.string.projects_expected_completion_footer),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.projects_active),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it }
                    )
                }
                Text(
                    text = stringResource(R.string.projects_inactive_footer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                thickness = 0.5.dp
            )

            Text(
                text = "${stringResource(R.string.projects_created_date)}: ${formatDate(project.createdDate)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${stringResource(R.string.projects_expenses)}: ${formatCurrencyAmount(state.totalExpenses, currency)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${stringResource(R.string.projects_income)}: ${formatCurrencyAmount(state.totalIncome, currency)}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun EditEndDatePickerRow(
    endDate: Long,
    onDateSelected: (Long) -> Unit
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.projects_end_date),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        TextButton(onClick = {
            val cal = Calendar.getInstance().apply { timeInMillis = endDate }
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
                text = formatDate(endDate),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun startOfDayMillis(timestamp: Long): Long = Calendar.getInstance().apply {
    timeInMillis = timestamp
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
