package com.yetzira.ContractorCashFlowAndroid.ui.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yetzira.ContractorCashFlowAndroid.data.local.AppDatabase
import com.yetzira.ContractorCashFlowAndroid.data.repository.ProjectRepository
import com.yetzira.ContractorCashFlowAndroid.sync.FirestoreSyncService

class ProjectViewModelFactory(
    private val database: AppDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProjectViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProjectViewModel(
                repository = ProjectRepository(
                    projectDao = database.projectDao(),
                    syncService = FirestoreSyncService(database)
                ),
                expenseDao = database.expenseDao(),
                invoiceDao = database.invoiceDao(),
                clientDao = database.clientDao()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

