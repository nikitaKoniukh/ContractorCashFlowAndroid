package com.yetzira.ContractorCashFlowAndroid.ui.labor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yetzira.ContractorCashFlowAndroid.data.local.AppDatabase
import com.yetzira.ContractorCashFlowAndroid.data.repository.LaborRepository
import com.yetzira.ContractorCashFlowAndroid.sync.FirestoreSyncService

class LaborViewModelFactory(
    private val database: AppDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LaborViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LaborViewModel(
                repository = LaborRepository(
                    laborDetailsDao = database.laborDetailsDao(),
                    expenseDao = database.expenseDao(),
                    projectDao = database.projectDao(),
                    syncService = FirestoreSyncService(database)
                )
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

