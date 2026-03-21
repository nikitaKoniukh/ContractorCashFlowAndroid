package com.yetzira.ContractorCashFlowAndroid.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yetzira.ContractorCashFlowAndroid.data.local.dao.ExpenseDao
import com.yetzira.ContractorCashFlowAndroid.data.local.dao.InvoiceDao
import com.yetzira.ContractorCashFlowAndroid.data.local.dao.ProjectDao
import com.yetzira.ContractorCashFlowAndroid.data.preferences.SettingsPreferencesRepositoryContract
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class AnalyticsViewModel(
    expenseDao: ExpenseDao,
    invoiceDao: InvoiceDao,
    projectDao: ProjectDao,
    userPreferencesRepository: SettingsPreferencesRepositoryContract
) : ViewModel() {

    private val selectedPeriod = MutableStateFlow(AnalyticsPeriod.MONTH)

    val uiState: StateFlow<AnalyticsUiState> = combine(
        selectedPeriod,
        expenseDao.getAll(),
        invoiceDao.getAll(),
        projectDao.getAll(),
        userPreferencesRepository.selectedCurrencyCode
    ) { period, expenses, invoices, projects, currency ->
        AnalyticsCalculator.buildUiState(
            selectedPeriod = period,
            currency = currency,
            expenses = expenses,
            invoices = invoices,
            projects = projects
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AnalyticsUiState()
    )

    fun setSelectedPeriod(period: AnalyticsPeriod) {
        selectedPeriod.value = period
    }
}

