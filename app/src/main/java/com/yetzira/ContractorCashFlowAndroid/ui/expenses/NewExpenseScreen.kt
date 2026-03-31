package com.yetzira.ContractorCashFlowAndroid.ui.expenses

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.yetzira.ContractorCashFlowAndroid.notification.BudgetWarningNotifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewExpenseScreen(
    viewModel: ExpenseViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val vmState by viewModel.formUiState.collectAsState()
    var formState by remember { mutableStateOf(vmState) }
    val saveResult by viewModel.lastSaveResult.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val preferencesRepository = remember(context) { UserPreferencesRepository(context.applicationContext) }
    val currency by preferencesRepository.selectedCurrencyCode.collectAsState(initial = CurrencyOption.ILS)
    val budgetHighMessage = stringResource(R.string.expenses_budget_warning_high)
    val budgetCriticalMessage = stringResource(R.string.expenses_budget_warning_critical)

    LaunchedEffect(Unit) {
        viewModel.startCreate()
    }

    LaunchedEffect(vmState.expenseId, vmState.workers, vmState.projects, vmState.date) {
        formState = viewModel.updateForm(vmState)
    }

    LaunchedEffect(saveResult) {
        when (val result = saveResult) {
            ExpenseSaveResult.None -> Unit
            ExpenseSaveResult.Saved -> {
                viewModel.resetSaveResult()
                onBack()
            }
            is ExpenseSaveResult.BudgetWarning -> {
                BudgetWarningNotifier(context).notify(
                    utilizationPercent = result.utilizationPercent,
                    projectName = result.projectName,
                    totalExpenses = result.totalExpenses,
                    budget = result.budget
                )
                snackbarHostState.showSnackbar(
                    if (result.utilizationPercent >= 100) {
                        budgetCriticalMessage
                    } else {
                        budgetHighMessage
                    }
                )
                viewModel.resetSaveResult()
                onBack()
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.expenses_new)) },
                navigationIcon = { TextButton(onClick = onBack) { Text(stringResource(R.string.common_back)) } },
                actions = {
                    TextButton(onClick = { viewModel.saveExpense(formState) }, enabled = formState.canSave) {
                        Text(stringResource(R.string.common_save))
                    }
                }
            )
        }
    ) { innerPadding ->
        ExpenseFormContent(
            state = formState,
            currency = currency,
            onStateChange = { updated -> formState = viewModel.updateForm(updated) },
            modifier = Modifier.padding(innerPadding).padding(16.dp)
        )
    }
}


