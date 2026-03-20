package com.yetzira.ContractorCashFlowAndroid.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yetzira.ContractorCashFlowAndroid.data.local.AppDatabase
import com.yetzira.ContractorCashFlowAndroid.data.repository.ExpenseRepository
import com.yetzira.ContractorCashFlowAndroid.sync.FirestoreSyncService

class ExpenseViewModelFactory(
    private val database: AppDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpenseViewModel(
                repository = ExpenseRepository(
                    expenseDao = database.expenseDao(),
                    projectDao = database.projectDao(),
                    laborDetailsDao = database.laborDetailsDao(),
                    syncService = FirestoreSyncService(database)
                )
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

