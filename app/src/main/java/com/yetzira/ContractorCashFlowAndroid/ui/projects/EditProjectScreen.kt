package com.yetzira.ContractorCashFlowAndroid.ui.projects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yetzira.ContractorCashFlowAndroid.R
import com.yetzira.ContractorCashFlowAndroid.data.preferences.CurrencyOption
import com.yetzira.ContractorCashFlowAndroid.data.preferences.UserPreferencesRepository
import com.yetzira.ContractorCashFlowAndroid.ui.components.formatAmountInput
import com.yetzira.ContractorCashFlowAndroid.ui.components.formatCurrencyAmount
import com.yetzira.ContractorCashFlowAndroid.ui.components.parseAmountInput
import com.yetzira.ContractorCashFlowAndroid.ui.components.ModernTextField
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun EditProjectScreen(
    projectId: String,
    viewModel: ProjectViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.detailUiState.collectAsState()
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
    var budget by remember(project.id) { mutableStateOf(formatAmountInput(project.budget.toLong().toString())) }

    val budgetValue = parseAmountInput(budget) ?: 0.0
    val reducedBelowExpenses = budgetValue in 0.0..<state.totalExpenses

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.common_edit)) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text(stringResource(R.string.common_back)) }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.updateProject(
                                project.copy(
                                    name = name,
                                    clientName = clientName,
                                    budget = budgetValue
                                )
                            )
                            onBack()
                        },
                        enabled = name.isNotBlank() && clientName.isNotBlank() && budgetValue > 0.0
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                    color = Color(0xFFFF3B30),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

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

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))


