package com.yetzira.ContractorCashFlowAndroid.ui.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yetzira.ContractorCashFlowAndroid.billing.PurchaseManagerProvider
import com.yetzira.ContractorCashFlowAndroid.data.local.AppDatabase
import com.yetzira.ContractorCashFlowAndroid.data.repository.ClientRepository
import com.yetzira.ContractorCashFlowAndroid.data.repository.ExpenseRepository
import com.yetzira.ContractorCashFlowAndroid.data.repository.InvoiceRepository
import com.yetzira.ContractorCashFlowAndroid.data.repository.ProjectRepository
import com.yetzira.ContractorCashFlowAndroid.sync.FirestoreSyncService

class ProjectViewModelFactory(
    private val database: AppDatabase,
    private val applicationContext: android.content.Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProjectViewModel::class.java)) {
            val syncService = FirestoreSyncService(database)
            @Suppress("UNCHECKED_CAST")
            return ProjectViewModel(
                repository = ProjectRepository(
                    projectDao = database.projectDao(),
                    expenseDao = database.expenseDao(),
                    syncService = syncService
                ),
                expenseDao = database.expenseDao(),
                invoiceDao = database.invoiceDao(),
                clientDao = database.clientDao(),
                clientRepository = ClientRepository(
                    clientDao = database.clientDao(),
                    projectDao = database.projectDao(),
                    invoiceDao = database.invoiceDao(),
                    syncService = syncService
                ),
                expenseRepository = ExpenseRepository(
                    expenseDao = database.expenseDao(),
                    projectDao = database.projectDao(),
                    laborDetailsDao = database.laborDetailsDao(),
                    syncService = syncService
                ),
                invoiceRepository = InvoiceRepository(
                    invoiceDao = database.invoiceDao(),
                    clientDao = database.clientDao(),
                    projectDao = database.projectDao(),
                    syncService = syncService
                ),
                purchaseManager = PurchaseManagerProvider.getInstance(applicationContext)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
