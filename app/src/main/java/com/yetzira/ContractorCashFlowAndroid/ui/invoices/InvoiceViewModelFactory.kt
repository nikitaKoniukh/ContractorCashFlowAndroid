package com.yetzira.ContractorCashFlowAndroid.ui.invoices

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yetzira.ContractorCashFlowAndroid.data.local.AppDatabase
import com.yetzira.ContractorCashFlowAndroid.data.preferences.UserPreferencesRepository
import com.yetzira.ContractorCashFlowAndroid.data.repository.InvoiceRepository
import com.yetzira.ContractorCashFlowAndroid.notification.InvoiceNotificationScheduler
import com.yetzira.ContractorCashFlowAndroid.sync.FirestoreSyncService

class InvoiceViewModelFactory(
    private val context: Context,
    private val database: AppDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InvoiceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InvoiceViewModel(
                repository = InvoiceRepository(
                    invoiceDao = database.invoiceDao(),
                    clientDao = database.clientDao(),
                    projectDao = database.projectDao(),
                    syncService = FirestoreSyncService(database)
                ),
                notificationScheduler = InvoiceNotificationScheduler(context),
                userPreferencesRepository = UserPreferencesRepository(context.applicationContext)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

